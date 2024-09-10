package com.ivan_degtev.telegrambotforpapablinov.component;

import com.ivan_degtev.telegrambotforpapablinov.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramWebhookConfiguration extends TelegramWebhookBot {

    private final BotConfig botConfig;

    public TelegramWebhookConfiguration(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public String getBotPath() {
        return botConfig.getWebhookPath();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Получено сообщение: " + update.getMessage().getText());
        }
        return null;
    }


    public void sendResponseMessage(String chatId, String text, Long replyToMessageId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        if (replyToMessageId != null) {
            message.setReplyToMessageId(replyToMessageId.intValue());
        }

        try {
            execute(message);
            log.info("Сообщение отправлено: " + text);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }
    public void sendReplyResponseMessage(String chatId, String text) {
        sendResponseMessage(chatId, text, null);
    }
}
