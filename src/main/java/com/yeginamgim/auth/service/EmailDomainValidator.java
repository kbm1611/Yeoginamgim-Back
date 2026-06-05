package com.yeginamgim.auth.service;

public interface EmailDomainValidator {
    boolean canReceiveMail(String email);
}
