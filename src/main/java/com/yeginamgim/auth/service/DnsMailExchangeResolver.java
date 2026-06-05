package com.yeginamgim.auth.service;

import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

@Component
public class DnsMailExchangeResolver {
    private static final String DNS_CONTEXT_FACTORY = "com.sun.jndi.dns.DnsContextFactory";

    public List<String> resolveMailServers(String domain) {
        String normalizedDomain = normalizeDomain(domain);
        if (normalizedDomain.isBlank()) {
            return List.of();
        }

        List<String> mxHosts = resolveMxHosts(normalizedDomain);
        if (!mxHosts.isEmpty()) {
            return mxHosts;
        }

        if (hasAddressRecord(normalizedDomain)) {
            return List.of(normalizedDomain);
        }

        return List.of();
    }

    private String normalizeDomain(String domain) {
        if (domain == null) {
            return "";
        }

        return domain.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> resolveMxHosts(String domain) {
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, DNS_CONTEXT_FACTORY);

        DirContext context = null;
        try {
            context = new InitialDirContext(environment);
            Attributes attributes = context.getAttributes(domain, new String[] {"MX"});
            Attribute mxRecords = attributes.get("MX");
            if (mxRecords == null || mxRecords.size() == 0) {
                return List.of();
            }

            List<MailExchange> exchanges = new ArrayList<>();
            for (int i = 0; i < mxRecords.size(); i++) {
                MailExchange exchange = parseMailExchange(mxRecords.get(i).toString());
                if (!exchange.host().isBlank()) {
                    exchanges.add(exchange);
                }
            }

            return exchanges.stream()
                    .sorted(Comparator.comparingInt(MailExchange::priority))
                    .map(MailExchange::host)
                    .toList();
        } catch (NamingException e) {
            return List.of();
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ignored) {
                    // Ignore close failures after DNS lookup.
                }
            }
        }
    }

    private MailExchange parseMailExchange(String value) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length >= 2) {
            return new MailExchange(parsePriority(parts[0]), stripTrailingDot(parts[1]));
        }

        return new MailExchange(Integer.MAX_VALUE, stripTrailingDot(value));
    }

    private int parsePriority(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private String stripTrailingDot(String host) {
        String normalizedHost = host.trim().toLowerCase(Locale.ROOT);
        if (normalizedHost.endsWith(".")) {
            return normalizedHost.substring(0, normalizedHost.length() - 1);
        }

        return normalizedHost;
    }

    private boolean hasAddressRecord(String domain) {
        try {
            return InetAddress.getAllByName(domain).length > 0;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private record MailExchange(int priority, String host) {
    }
}
