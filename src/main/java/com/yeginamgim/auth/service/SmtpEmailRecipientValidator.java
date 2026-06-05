package com.yeginamgim.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SmtpEmailRecipientValidator implements EmailDomainValidator {
    private final DnsMailExchangeResolver mailExchangeResolver;
    private final SmtpRecipientProbe smtpRecipientProbe;

    @Override
    public boolean canReceiveMail(String email) {
        String normalizedEmail = normalizeEmail(email);
        String domain = extractDomain(normalizedEmail);
        if (domain.isBlank()) {
            return false;
        }

        List<String> mailServers = mailExchangeResolver.resolveMailServers(domain);
        if (mailServers.isEmpty()) {
            return false;
        }

        return mailServers.stream()
                .anyMatch(mailServer -> smtpRecipientProbe.acceptsRecipient(mailServer, normalizedEmail));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "";
        }

        String domain = email.substring(atIndex + 1);
        if (!domain.contains(".") || domain.contains("..")) {
            return "";
        }

        return domain;
    }
}
