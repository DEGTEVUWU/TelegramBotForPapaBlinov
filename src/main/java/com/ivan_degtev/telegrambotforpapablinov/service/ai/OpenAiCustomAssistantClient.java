package com.ivan_degtev.telegrambotforpapablinov.service.ai;

import com.ivan_degtev.telegrambotforpapablinov.exception.LlmQuerySyntaxException;
import com.ivan_degtev.telegrambotforpapablinov.mapper.OpenAiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenAiCustomAssistantClient {

    @Value("${openai.token}")
    private String openAiToken;
    private final WebClient webClient;
    private final OpenAiMapper openAiMapper;

    @Lazy private final OpenAiMemoryControlServiceImpl openAiMemoryControlService;

    private final static String assistantId = "asst_TMo9HU85ItAzi87f2fMeSheQ";

    private final static Map<Long, String> userThreads = new HashMap<>();
    private final static Map<Long, Integer> summaryMessagesForUser = new HashMap<>();

    public OpenAiCustomAssistantClient(
            @Value("${openai.token}") String openAiToken,
            OpenAiMapper openAiMapper,
            WebClient.Builder webClient,
            @Lazy OpenAiMemoryControlServiceImpl openAiMemoryControlService
    ) {
        this.openAiToken = openAiToken;
        this.openAiMapper = openAiMapper;
        this.webClient = webClient
                .baseUrl("https://api.openai.com")
                .build();
        this.openAiMemoryControlService = openAiMemoryControlService;
    }

    public String createRequestGetResponse(Long fromId, String question) {
        try {
            String threadId = userThreads.get(fromId);
            if (threadId == null) {
                String jsonResponseCreateThread = createThread(fromId);
                threadId = openAiMapper.extractIdAfterCreateResponseMessage(jsonResponseCreateThread);

                userThreads.put(fromId, threadId);
                addAndCleanHistoryMessage(fromId);
            } else {
                addAndCleanHistoryMessage(fromId);
                threadId = userThreads.get(fromId);
            }

            String jsonResponseCreateMessage = createResponseMessage(threadId, question);
            String responseMessageId = openAiMapper.extractIdAfterCreateResponseMessage(jsonResponseCreateMessage); //id сообщения - вопроса

            String jsonResponseCreateRun = runThread(threadId);
            String responseRunId = openAiMapper.extractIdAfterCreateResponseMessage(jsonResponseCreateRun);


            if (checkStatusReceivingResponse(threadId, responseRunId)) {
                String jsonResponseGetMessages = getMessages(threadId);
                String responseIdAnswer = openAiMapper.extractLatestMessageId(jsonResponseGetMessages);

                String jsonResponseGetMessage = getMessage(threadId, responseIdAnswer);
                String responseLlm = openAiMapper.extractDataFromLlmAnswer(jsonResponseGetMessage);
                log.info("Получил ответ от ллм по сути вопроса замапленный: {}", responseLlm);

                return responseLlm;
            }
        } catch (LlmQuerySyntaxException ex) {
            throw new LlmQuerySyntaxException("Ошибка в последовательности запросов к Open AI для работы с ассистентом");
        }
        throw new LlmQuerySyntaxException("Ошибка в последовательности запросов к Open AI для работы с ассистентом");
    }

    /**
     * метод проверяет кол-во записей в мапе по id чата, когда оно равно 10  - создаётся новый тред и отсчет начинается заново(для экономии токенов)
     */
    public void addAndCleanHistoryMessage(Long chatId) {
        if (summaryMessagesForUser.get(chatId) == null) {
            summaryMessagesForUser.put(chatId, 1);
        } else if (summaryMessagesForUser.get(chatId) != null && summaryMessagesForUser.get(chatId) < 10) {
            summaryMessagesForUser.put(chatId, summaryMessagesForUser.get(chatId) + 1);
        } else if (summaryMessagesForUser.get(chatId) != null && summaryMessagesForUser.get(chatId) == 10) {
            String summaryMessages = openAiMemoryControlService.generateManualSummary(userThreads.get(chatId));

            deleteOldThread(userThreads.get(chatId));

            String jsonResponseCreateThread = createNewThreadWithSummary(summaryMessages);
            String threadId = openAiMapper.extractIdAfterCreateResponseMessage(jsonResponseCreateThread);

            userThreads.put(chatId, threadId);
            summaryMessagesForUser.put(chatId, 1);
            log.info("Записан новый id треда после переполнения прошлого {}", userThreads.get(chatId));
        }
    }
    /**
     * Метод проверяет внесен ли в общий тред ответ от ллм, когда это происходит - он отдаёт айди этого сообщения
     * @return
     */
    public boolean checkStatusReceivingResponse(String threadId, String responseRunId) {
        Instant startTime = Instant.now();
        String responseRunStatus;

        do {
            String jsonResponseGetRun = getRun(threadId, responseRunId);
            responseRunStatus = openAiMapper.extractRunStatus(jsonResponseGetRun);

            if (responseRunStatus.equals("completed")) {
                return true;
            }

            if (Duration.between(startTime, Instant.now()).getSeconds() > 60) {
                log.warn("Timeout reached while waiting for completion status.");
                throw new LlmQuerySyntaxException("Время ожидания превышено, статус не стал 'completed'.");
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for completion status", e);
                return false;
            }
        } while (!responseRunStatus.equals("completed"));

        return false;
    }


    /**
     * Утилитный метод для создания нового треда для экономии токенов и передачи в него резюме прошлого контекста общения, срабатывает после 10 вопросов в текущем треде
     * @param summary
     * @return
     */
    public String createNewThreadWithSummary(String summary) {
        String response = webClient.post()
                .uri("/v1/threads")
                .header("Authorization", "Bearer " + openAiToken)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "assistant_id", assistantId,
                        "messages", List.of(
                                Map.of("role", "assistant", "content", "Вот что мы обсудили на данный момент: " + summary)
                        )
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;
    }

    public String deleteOldThread(String threadId) {
        String response = webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/threads/{threadId}")
                        .build(threadId))
                .header("Authorization", "Bearer " + openAiToken)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;
    }

    public String getCompanyAssistants() {
        return webClient.get()
                .uri("/v1/assistants")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     *     Метод для создания треда для конкретного пользователя
     */
    public String createThread(Long chatId) {
        return webClient.post()
                .uri("/v1/threads")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }

    /**
     * Метод по созданию сообщения-вопрос к ллм, для помещения его после в тред
     */
    public String createResponseMessage(String threadId, String userMessage) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/threads/{threadId}/messages")
                        .build(threadId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("role", "user", "content", userMessage))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Метод для запуска треда, в который добавлено текущее сообщение. Важно. Получить айди этого запуска для дальнейшей работы
     * @param threadId
     * @return
     */
    public String runThread(String threadId) {
        log.info("Running thread with ID: {}", threadId);
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/threads/{threadId}/runs")
                            .build(threadId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                    .header("OpenAI-Beta", "assistants=v2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("assistant_id", assistantId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error response from server: {}", e.getResponseBodyAsString());
            log.error("Status code: {}, message: {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            return null;
        }
    }


    /**
     * Метод для просмотра инфы о запуске. Использвоать только статус - при завершённом - получить далее изменения в треде - то есть ответ
     * @param threadId
     * @param runId - получить после запуска
     * @return
     */
    public String getRun(String threadId, String runId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/threads/{threadId}/runs/{runId}")
                        .build(threadId, runId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Получить все сообщения из треда. Использвоать, когда нужно получить айди последнего сообщения - ответа от ллм
     * @param threadId
     * @return
     */
    public String getMessages(String threadId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/threads/{threadId}/messages")
                        .build(threadId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getMessage(String threadId, String messageId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/threads/{threadId}/messages/{messageId}")
                        .build(threadId, messageId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}