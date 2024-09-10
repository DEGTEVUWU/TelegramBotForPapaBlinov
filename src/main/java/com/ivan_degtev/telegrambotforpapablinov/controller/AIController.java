package com.ivan_degtev.telegrambotforpapablinov.controller;

import com.ivan_degtev.telegrambotforpapablinov.mapper.OpenAiMapper;
import com.ivan_degtev.telegrambotforpapablinov.service.ai.OpenAiCustomAssistantClient;
import com.ivan_degtev.telegrambotforpapablinov.service.ai.OpenAiMemoryControlServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/ai")
public class AIController {

    private final OpenAiCustomAssistantClient openAiCustomAssistantClient;
    private final OpenAiMemoryControlServiceImpl openAiMemoryControlService;
    private final OpenAiMapper openAiMapper;

    @GetMapping(path = "/assistants")
    public ResponseEntity<String> getCompanyAssistants() {
        String response = openAiCustomAssistantClient.getCompanyAssistants();
        return ResponseEntity
                .ok()
                .body(response);
    }
    @PostMapping(path = "/summary/{threadId}")
    public ResponseEntity<String> createSummary(@PathVariable String threadId) {
        String response = openAiMemoryControlService.generateManualSummary(threadId);
        return ResponseEntity
                .ok()
                .body(response);
    }
    @PostMapping(path = "/create_thread_with_summary")
    public ResponseEntity<String> createThreadWithSummary(@RequestBody String summary) {
        String response = openAiCustomAssistantClient.createNewThreadWithSummary(summary);
        return ResponseEntity
                .ok()
                .body(response);
    }
}
