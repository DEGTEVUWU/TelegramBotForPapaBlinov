package com.ivan_degtev.telegrambotforpapablinov.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WebhookPayloadDto {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private MessageDto message;

    @Data
    public static class MessageDto {

        @JsonProperty("message_id")
        private Long messageId;

        @JsonProperty("from")
        private UserDto from;

        @JsonProperty("chat")
        private ChatDto chat;

        private Long date;
        private String text;

        @JsonProperty("entities")
        private List<EntityDto> entities;
    }

    @Data
    public static class UserDto {
        private Long id;

        @JsonProperty("is_bot")
        private boolean isBot;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        private String username;

        @JsonProperty("language_code")
        private String languageCode;
    }

    @Data
    public static class ChatDto {
        private Long id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        private String username;
        private String type;
    }

    @Data
    public static class EntityDto {
        private int offset;
        private int length;
        private String type;
    }
}
