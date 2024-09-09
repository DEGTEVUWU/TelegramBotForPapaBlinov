package com.ivan_degtev.telegrambotforpapablinov.service;

import com.ivan_degtev.telegrambotforpapablinov.dto.mapping.WebhookPayloadDto;
import org.springframework.stereotype.Service;

@Service
public interface TriggersForBotService {


    /**
     * Общий метод для подготовки нужных данных и отпраувки в нужные триггеры для првоерки
     *
     * @return
     */
    boolean handleMessage(WebhookPayloadDto payload);

    /**
    Метод для проверки упоминается ли бот по нику
     */
    boolean isBotMentioned(WebhookPayloadDto.MessageDto message, String botUsername);

    /**
     * Метод для првоерки каждого сообщения в чате - ответ ли это на другие сообщения бота
     */
    boolean isReplyToBot(WebhookPayloadDto.MessageDto message, Long botId);
}
