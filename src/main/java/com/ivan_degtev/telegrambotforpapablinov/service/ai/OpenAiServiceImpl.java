package com.ivan_degtev.telegrambotforpapablinov.service.ai;

import com.ivan_degtev.telegrambotforpapablinov.component.TelegramWebhookConfiguration;
import com.ivan_degtev.telegrambotforpapablinov.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    private final Assistant assistant;
    private final TelegramWebhookConfiguration telegramWebhookConfiguration;
    private final OpenAiCustomAssistantClient openAiCustomAssistantClient;

    @Override
    public void getAnswerFromLlm(String chatId, String fromId, String question, Long replayMessageId) {
//        String llmAnswer = assistant.chat(fromId, chatId, question);
        String llmAnswer = openAiCustomAssistantClient.createRequestGetResponse(Long.valueOf(fromId), question);
        log.info("Ответ от ллм {}", llmAnswer);

        sendMessage(chatId, llmAnswer, replayMessageId);
    }

    @Override
    public void sendMessage(String chatId, String llmAnswer, Long replayMessageId) {

        telegramWebhookConfiguration.sendResponseMessage(chatId, llmAnswer, replayMessageId);

    }
}
