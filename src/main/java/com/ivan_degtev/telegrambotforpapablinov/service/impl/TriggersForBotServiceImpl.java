package com.ivan_degtev.telegrambotforpapablinov.service.impl;

import com.ivan_degtev.telegrambotforpapablinov.dto.mapping.WebhookPayloadDto;
import com.ivan_degtev.telegrambotforpapablinov.service.TriggersForBotService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TriggersForBotServiceImpl implements TriggersForBotService {

    @Override
    public boolean handleMessage(WebhookPayloadDto payload) {
        WebhookPayloadDto.MessageDto message = payload.getMessage();
        String chatType = payload.getMessage().getChat().getType();

        /*
        Ранняя првоерка, если чат приватный - отвечать на все сообщения
         */
        if (chatType.equals("private")) {
            return true;
        } else if (chatType.equals("supergroup")) {
            var replyToMessage = payload.getMessage().getReplyToMessage();

            Long botId = null;
            String botUsername = "papa_blinov_bot";

            if (replyToMessage != null) {
                botId = replyToMessage.getFrom() != null ? replyToMessage.getFrom().getId() : null;
            }

            return isBotMentioned(message, botUsername) || (botId != null && isReplyToBot(message, botId));
        }
        return false;
    }

    @Override
    public boolean isBotMentioned(WebhookPayloadDto.MessageDto message, String botUsername) {
        if (message.getEntities() != null) {
            for (WebhookPayloadDto.EntityDto entity : message.getEntities()) {
                if ("mention".equals(entity.getType())) {
                    String mentionText = message.getText().substring(entity.getOffset(), entity.getOffset() + entity.getLength());

                    if (mentionText.startsWith("@") && mentionText.substring(1).equalsIgnoreCase(botUsername)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isReplyToBot(WebhookPayloadDto.MessageDto message, Long botId) {
        WebhookPayloadDto.MessageDto replyToMessage = message.getReplyToMessage();
        if (replyToMessage != null) {
            return replyToMessage.getFrom() != null && replyToMessage.getFrom().getId().equals(botId);
        }
        return false;
    }
}
