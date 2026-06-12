package com.yeginamgim.trace.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProfanityFilterServiceTest {

    private final ProfanityFilterClient profanityFilterClient = mock(ProfanityFilterClient.class);
    private final ProfanityProperties properties = new ProfanityProperties();
    private final ProfanityFilterService profanityFilterService = new ProfanityFilterService(
            profanityFilterClient,
            properties
    );

    @Test
    void validateTextsSkipsClientWhenTextListIsEmpty() {
        profanityFilterService.validateTexts(List.of());

        verifyNoInteractions(profanityFilterClient);
    }

    @Test
    void validateTextsAllowsCleanResponse() {
        when(profanityFilterClient.check(List.of("좋은 기억")))
                .thenReturn(new ProfanityCheckResponse(
                        false,
                        List.of(new ProfanityCheckResponse.Result(false, 0.02))
                ));

        assertThatCode(() -> profanityFilterService.validateTexts(List.of("좋은 기억")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateTextsRejectsWhenBlockedIsTrue() {
        when(profanityFilterClient.check(List.of("나쁜 말")))
                .thenReturn(new ProfanityCheckResponse(true, List.of()));

        assertThatThrownBy(() -> profanityFilterService.validateTexts(List.of("나쁜 말")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void validateTextsRejectsWhenAnyResultIsProfanity() {
        when(profanityFilterClient.check(List.of("나쁜 말")))
                .thenReturn(new ProfanityCheckResponse(
                        false,
                        List.of(new ProfanityCheckResponse.Result(true, 0.91))
                ));

        assertThatThrownBy(() -> profanityFilterService.validateTexts(List.of("나쁜 말")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("부적절한 표현이 포함되어 있습니다.");
    }

    @Test
    void validateTextsRejectsOnClientFailureWhenFailOpenIsFalse() {
        properties.setFailOpen(false);
        when(profanityFilterClient.check(List.of("검사 문장")))
                .thenThrow(new ProfanityFilterException("filter unavailable"));

        assertThatThrownBy(() -> profanityFilterService.validateTexts(List.of("검사 문장")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("부적절한 표현을 확인할 수 없습니다.");
    }

    @Test
    void validateTextsAllowsOnClientFailureWhenFailOpenIsTrue() {
        properties.setFailOpen(true);
        when(profanityFilterClient.check(List.of("검사 문장")))
                .thenThrow(new ProfanityFilterException("filter unavailable"));

        assertThatCode(() -> profanityFilterService.validateTexts(List.of("검사 문장")))
                .doesNotThrowAnyException();
    }
}
