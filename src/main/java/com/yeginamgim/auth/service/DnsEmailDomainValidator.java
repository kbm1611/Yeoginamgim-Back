package com.yeginamgim.auth.service;

import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

@Component
public class DnsEmailDomainValidator implements EmailDomainValidator {
    private static final String DNS_CONTEXT_FACTORY = "com.sun.jndi.dns.DnsContextFactory";

    @Override
    public boolean canReceiveMail(String email) {
        String domain = extractDomain(email);
        if (domain.isBlank()) {
            return false;
        }

        return hasMxRecord(domain) || hasAddressRecord(domain);
    }

    private String extractDomain(String email) {
        if (email == null) {
            return "";
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return "";
        }

        return email.substring(atIndex + 1).trim();
    }

    private boolean hasMxRecord(String domain) {
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, DNS_CONTEXT_FACTORY);

        try {
            Attributes attributes = new InitialDirContext(environment).getAttributes(domain, new String[] {"MX"});
            Attribute mxRecords = attributes.get("MX");
            return mxRecords != null && mxRecords.size() > 0;
        } catch (NamingException e) {
            return false;
        }
    }

    private boolean hasAddressRecord(String domain) {
        try {
            return InetAddress.getAllByName(domain).length > 0;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
