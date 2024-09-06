package com.ivan_degtev.telegrambotforpapablinov.service;

import org.springframework.stereotype.Service;

@Service
public interface OpenAiService {

     void sendMessage(String chatId, String question);
}
