package com.ivan_degtev.telegrambotforpapablinov.service.impl;

import com.ivan_degtev.telegrambotforpapablinov.dto.mapping.WebhookPayloadDto;
import com.ivan_degtev.telegrambotforpapablinov.mapper.WebhookMapper;
import com.ivan_degtev.telegrambotforpapablinov.service.TelegramService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.Webhook;

@Service
@Slf4j
@AllArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final WebhookMapper webhookMapper;
    private final OpenAiServiceImpl openAiServiceImpl;

    @Override
    public void getNewMessagesFromWebhook(String payload) {

        WebhookPayloadDto webhookPayloadDto = webhookMapper.convertStringToWebhookPayload(payload);
        String chatId = String.valueOf(webhookPayloadDto.getMessage().getChat().getId());
        String textMessage = webhookPayloadDto.getMessage().getText();

        openAiServiceImpl.sendMessage(chatId, textMessage);

    }
}
