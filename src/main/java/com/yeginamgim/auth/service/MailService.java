package com.yeginamgim.auth.service;

import com.yeginamgim.global.exception.EmailVerificationMailException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MailService {
    private static final String VERIFICATION_SUBJECT = "[여기남김] 이메일 인증번호 안내";

    private final JavaMailSender mailSender;
    private final EmailDomainValidator emailDomainValidator;

    public void sendVerificationCode(String email, String code, Duration expiresIn) {
        String recipient = email.trim();
        if (!emailDomainValidator.canReceiveMail(recipient)) {
            throw new EmailVerificationMailException(new IllegalArgumentException("Email domain cannot receive mail."));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(VERIFICATION_SUBJECT);
        message.setText(buildVerificationText(code, expiresIn));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailVerificationMailException(e);
        }
    }

    private String buildVerificationText(String code, Duration expiresIn) {
        return """
                안녕하세요. 여기남김입니다.

                이메일 인증번호는 %s 입니다.
                인증번호 만료 시간은 %s입니다.

                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(code, formatDuration(expiresIn));
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes > 0 && duration.minusMinutes(minutes).isZero()) {
            return minutes + "분";
        }
        return duration.toSeconds() + "초";
    }
}
