package com.ivan_degtev.telegrambotforpapablinov.service.ai;

import com.ivan_degtev.telegrambotforpapablinov.mapper.OpenAiMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Service
@Slf4j
public class ProcessingSearchRequestsService {

    @Value("${openai.token}")
    private String openAiToken;
    private final WebClient webClient;
    private final OpenAiMapper openAiMapper;

    private final static String ASSISTANT_ID = "asst_TMo9HU85ItAzi87f2fMeSheQ";
    private final static String VECTOR_STORE_ID = "vs_wNzBgdtEtTf1cT1G6GifHZSn";
    private final static String FILES_SAVE_PATH = "src/main/resources/files/";

    public ProcessingSearchRequestsService(
            @Value("${openai.token}") String openAiToken,
            OpenAiMapper openAiMapper,
            WebClient.Builder webClient
    ) {
        this.openAiToken = openAiToken;
        this.openAiMapper = openAiMapper;
        this.webClient = webClient
                .baseUrl("https://api.openai.com")
                .build();
    }

    public void preparingDataForDownloadingFiles(Map<String, String> filesData) {
        for (Map.Entry<String, String> file : filesData.entrySet()) {
            downloadFile(file.getKey(), file.getValue());
        }
    }

    public void downloadFile(String fileName, String fileId) {
        try {
            byte[] fileContent = webClient.get()
                    .uri("/v1/files/{fileId}/content", fileId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiToken)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (fileContent != null) {
                saveFile(fileContent, fileName);
            } else {
                System.err.println("Файл не был получен.");
            }
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка при скачивании файла: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при скачивании файла: " + e.getMessage());
        }
    }

    private void saveFile(byte[] fileContent, String fileName) throws IOException {
        Path path = Paths.get(FILES_SAVE_PATH + fileName);
        Files.write(path, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("Файл успешно сохранен по пути: " + path);
    }
}
