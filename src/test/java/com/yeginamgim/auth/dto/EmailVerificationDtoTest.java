package com.yeginamgim.auth.dto;

import com.yeginamgim.auth.dto.request.EmailVerificationSendRequest;
import com.yeginamgim.auth.dto.request.EmailVerificationVerifyRequest;
import com.yeginamgim.auth.dto.response.EmailVerificationResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void sendRequestRequiresValidEmail() {
        EmailVerificationSendRequest blankEmail = EmailVerificationSendRequest.builder()
                .email("")
                .build();
        EmailVerificationSendRequest invalidEmail = EmailVerificationSendRequest.builder()
                .email("not-an-email")
                .build();

        assertThat(messagesOf(blankEmail)).contains("email is required.");
        assertThat(messagesOf(invalidEmail)).contains("email must be valid.");
    }

    @Test
    void verifyRequestRequiresValidEmailAndSixDigitCode() {
        EmailVerificationVerifyRequest request = EmailVerificationVerifyRequest.builder()
                .email("not-an-email")
                .code("12ab56")
                .build();

        assertThat(messagesOf(request))
                .contains("email must be valid.", "code must be 6 digits.");
    }

    @Test
    void verificationResponseHasMessageAndVerifiedFields() {
        EmailVerificationResponse response = EmailVerificationResponse.builder()
                .message("verification completed.")
                .verified(true)
                .build();

        assertThat(response.getMessage()).isEqualTo("verification completed.");
        assertThat(response.isVerified()).isTrue();
    }

    private Set<String> messagesOf(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}
