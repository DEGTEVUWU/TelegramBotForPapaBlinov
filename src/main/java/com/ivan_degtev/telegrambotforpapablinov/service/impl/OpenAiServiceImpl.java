package com.ivan_degtev.telegrambotforpapablinov.service.impl;

import com.ivan_degtev.telegrambotforpapablinov.component.TelegramWebhookConfiguration;
import com.ivan_degtev.telegrambotforpapablinov.service.OpenAiService;
import com.ivan_degtev.telegrambotforpapablinov.service.ai.Assistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.spec.OAEPParameterSpec;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    private final Assistant assistant;
    private final TelegramWebhookConfiguration telegramWebhookConfiguration;
    @Override
    public void sendMessage(String chatId, String question) {
        String llmAnswer = assistant.chat(chatId, chatId, question);
        log.info("Ответ от ллм {}", llmAnswer);

        telegramWebhookConfiguration.sendResponseMessage(chatId, llmAnswer);

    }
}
