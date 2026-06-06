package com.yeginamgim.auth.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SmtpEmailRecipientValidatorTest {

    private final DnsMailExchangeResolver mailExchangeResolver = mock(DnsMailExchangeResolver.class);
    private final SmtpRecipientProbe smtpRecipientProbe = mock(SmtpRecipientProbe.class);
    private final SmtpEmailRecipientValidator validator = new SmtpEmailRecipientValidator(
            mailExchangeResolver,
            smtpRecipientProbe
    );

    @Test
    void acceptsRecipientWhenAnyResolvedMailServerAcceptsRcptTo() {
        when(mailExchangeResolver.resolveMailServers("gmail.com"))
                .thenReturn(List.of("mx1.gmail.com", "mx2.gmail.com"));
        when(smtpRecipientProbe.acceptsRecipient("mx1.gmail.com", "user@gmail.com")).thenReturn(false);
        when(smtpRecipientProbe.acceptsRecipient("mx2.gmail.com", "user@gmail.com")).thenReturn(true);

        assertThat(validator.canReceiveMail(" USER@gmail.com ")).isTrue();
    }

    @Test
    void rejectsRecipientWhenEveryResolvedMailServerRejectsRcptTo() {
        when(mailExchangeResolver.resolveMailServers("gmail.com"))
                .thenReturn(List.of("mx1.gmail.com", "mx2.gmail.com"));
        when(smtpRecipientProbe.acceptsRecipient("mx1.gmail.com", "missing@gmail.com")).thenReturn(false);
        when(smtpRecipientProbe.acceptsRecipient("mx2.gmail.com", "missing@gmail.com")).thenReturn(false);

        assertThat(validator.canReceiveMail("missing@gmail.com")).isFalse();
    }

    @Test
    void rejectsRecipientWhenNoMailServerCanBeResolved() {
        when(mailExchangeResolver.resolveMailServers("missing-domain.invalid")).thenReturn(List.of());

        assertThat(validator.canReceiveMail("user@missing-domain.invalid")).isFalse();

        verifyNoInteractions(smtpRecipientProbe);
    }

    @Test
    void rejectsMalformedEmailBeforeDnsOrSmtpLookup() {
        assertThat(validator.canReceiveMail("2221@11")).isFalse();

        verifyNoInteractions(mailExchangeResolver, smtpRecipientProbe);
    }
}
