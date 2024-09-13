package com.ivan_degtev.telegrambotforpapablinov.service.impl;

import com.ivan_degtev.telegrambotforpapablinov.dto.TYPE_REQUEST;
import com.ivan_degtev.telegrambotforpapablinov.service.UpdateIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements UpdateIdService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "update_id:";
    private static final String FROM_ID = "from_id:";

    private static final String THREADS_KEY = "user:threads";
    private static final String MESSAGES_COUNT_KEY = "user:messagesCount";
    /**
     * Для фильтрации множественной отправки дублей из нгрока, пропускает только 1 апдейт с уник айди
     * @param updateId
     * @return
     */
    public boolean isUniqueUpdateId(Long updateId) {
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + updateId, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(isNew);
    }

    /**
     * Сохраняет тип запроса для каждою юзера - нужно для понимая идет обычный диалог или запрос на выкачку файлов
     */
    public void saveUserRequestType(String fromId, TYPE_REQUEST typeRequest) {
        redisTemplate.opsForValue().set(FROM_ID + fromId, typeRequest, Duration.ofHours(72));
        log.info("Сохранен тип запроса для пользователя {}: {}", fromId, typeRequest);
    }

    /**
     * Возвращает текущий тип диалога для дальнейше работой с ллм
     */
    public TYPE_REQUEST getUserRequestType(String fromId) {
        TYPE_REQUEST typeRequest = (TYPE_REQUEST) redisTemplate.opsForValue().get(FROM_ID + fromId);

        if (typeRequest != null) {
            log.info("Найден тип запроса для пользователя {}: {}", fromId, typeRequest);
            return typeRequest;
        } else {
            log.info("Тип запроса для пользователя {} не найден в Redis", fromId);
            return TYPE_REQUEST.DEFAULT;
        }
    }

    /**
     * Методы для сохранения данных о тредах для юзера и коол-ве сообщений, нужно для контроля и удаления старых тредов, когда кол-во сообщений в текущем превысит 10
     */
    // Получение треда пользователя
    public String getUserThread(String fromId) {
        return (String) redisTemplate.opsForHash().get(THREADS_KEY, fromId);
    }

    // Сохранение треда пользователя
    public void setUserThread(String fromId, String threadId) {
        redisTemplate.opsForHash().put(THREADS_KEY, fromId, threadId);
    }

    // Получение количества сообщений пользователя
    public int getUserMessageCount(String fromId) {
        String count = (String) redisTemplate.opsForHash().get(MESSAGES_COUNT_KEY, fromId);
        return count != null ? Integer.parseInt(count) : 0;
    }

    // Инкремент количества сообщений
    public void incrementUserMessageCount(String fromId) {
        redisTemplate.opsForHash().increment(MESSAGES_COUNT_KEY, fromId, 1);
    }

    // Сброс количества сообщений
    public void resetUserMessageCount(String fromId) {
        redisTemplate.opsForHash().put(MESSAGES_COUNT_KEY, fromId, "1");
    }

    // Удаление старого треда и связанной информации
    public void deleteOldThread(String fromId) {
        redisTemplate.opsForHash().delete(THREADS_KEY, fromId);
        redisTemplate.opsForHash().delete(MESSAGES_COUNT_KEY, fromId);
    }
}
