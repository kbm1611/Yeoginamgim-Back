package com.yeginamgim.auth.service;

import com.yeginamgim.global.exception.EmailVerificationMailException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailService mailService = new MailService(mailSender);

    @Test
    void sendVerificationCodeSendsKoreanMailWithCodeAndExpiration() {
        mailService.sendVerificationCode("user@example.com", "123456", Duration.ofMinutes(5));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getSubject()).contains("이메일 인증번호");
        assertThat(message.getText()).contains("123456", "5분", "인증번호");
    }

    @Test
    void sendVerificationCodeThrowsConsistentExceptionWhenMailSendingFails() {
        doThrow(new MailSendException("mail failed")).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        assertThatThrownBy(() -> mailService.sendVerificationCode(
                "user@example.com",
                "123456",
                Duration.ofMinutes(5)
        )).isInstanceOf(EmailVerificationMailException.class)
                .hasMessage("이메일 인증번호 발송에 실패했습니다.");
    }
}
