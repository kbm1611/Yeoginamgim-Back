package com.yeginamgim.trace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfanityFilterService {

    private static final String BLOCKED_MESSAGE = "부적절한 표현이 포함되어 있습니다.";
    private static final String UNAVAILABLE_MESSAGE = "부적절한 표현을 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.";

    private final ProfanityFilterClient profanityFilterClient;
    private final ProfanityProperties properties;

    public void validateTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return;
        }

        int textCount = texts.size();
        int totalLength = texts.stream().mapToInt(String::length).sum();

        try {
            ProfanityCheckResponse response = profanityFilterClient.check(texts);
            if (isBlocked(response)) {
                log.warn(
                        "Profanity filter blocked trace text. textCount={}, totalLength={}, blocked={}, resultCount={}",
                        textCount,
                        totalLength,
                        response != null && response.blocked(),
                        resultCount(response)
                );
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BLOCKED_MESSAGE);
            }

            log.info(
                    "Profanity filter allowed trace text. textCount={}, totalLength={}, resultCount={}",
                    textCount,
                    totalLength,
                    resultCount(response)
            );
        } catch (ProfanityFilterException exception) {
            if (properties.isFailOpen()) {
                log.warn(
                        "Profanity filter unavailable; fail-open allowed trace text. textCount={}, totalLength={}, errorType={}",
                        textCount,
                        totalLength,
                        exception.getClass().getSimpleName()
                );
                return;
            }

            log.warn(
                    "Profanity filter unavailable; fail-closed rejected trace text. textCount={}, totalLength={}, errorType={}",
                    textCount,
                    totalLength,
                    exception.getClass().getSimpleName()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, UNAVAILABLE_MESSAGE);
        }
    }

    private boolean isBlocked(ProfanityCheckResponse response) {
        if (response == null) {
            throw new ProfanityFilterException("Profanity API returned empty response.");
        }

        if (response.blocked()) {
            return true;
        }

        return response.results() != null
                && response.results().stream()
                .anyMatch(result -> result != null && result.isProfanity());
    }

    private int resultCount(ProfanityCheckResponse response) {
        return response == null || response.results() == null ? 0 : response.results().size();
    }
}
