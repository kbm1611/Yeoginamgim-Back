package com.yeginamgim.auth.service;

import com.yeginamgim.global.exception.EmailVerificationMailException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MailService {
    private static final String VERIFICATION_SUBJECT = "[여기남김] 이메일 인증번호 안내";
    private static final String ACTIVITY_RESTRICTION_SUBJECT = "[여기남김] 일부 활동이 7일간 제한되었습니다";
    private static final DateTimeFormatter RESTRICTION_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Seoul"));

    private final JavaMailSender mailSender;
    private final EmailDomainValidator emailDomainValidator;

    @Value("${email.verification.recipient-validation.enabled:false}")
    private boolean recipientValidationEnabled;

    public void sendVerificationCode(String email, String code, Duration expiresIn) {
        String recipient = email.trim();
        if (recipientValidationEnabled && !emailDomainValidator.canReceiveMail(recipient)) {
            throw new EmailVerificationMailException(new IllegalArgumentException("Email recipient cannot receive mail."));
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

    public void sendActivityRestrictionNotice(String email, Instant restrictedUntil) {
        String recipient = email.trim();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(ACTIVITY_RESTRICTION_SUBJECT);
        message.setText(buildActivityRestrictionText(restrictedUntil));

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

    private String buildActivityRestrictionText(Instant restrictedUntil) {
        return """
                안녕하세요. 여기남김입니다.

                최근 7일 동안 신고 누적으로 숨김 처리된 흔적이 3개 이상 발생하여
                흔적 작성, 좋아요, 신고 등 일부 활동이 7일간 제한되었습니다.

                로그인과 조회 기능은 계속 이용할 수 있습니다.

                활동 제한 해제 예정일: %s
                """.formatted(RESTRICTION_TIME_FORMATTER.format(restrictedUntil));
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes > 0 && duration.minusMinutes(minutes).isZero()) {
            return minutes + "분";
        }
        return duration.toSeconds() + "초";
    }
}
