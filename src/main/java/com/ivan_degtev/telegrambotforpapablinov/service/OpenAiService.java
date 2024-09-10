package com.ivan_degtev.telegrambotforpapablinov.service;

import org.springframework.stereotype.Service;

@Service
public interface OpenAiService {

     void getAnswerFromLlm(String chatId, String fromId, String question, Long replayMessageId);
     void sendMessage(String chatId, String answer, Long replayMessageId);
}
