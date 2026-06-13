package com.yeginamgim.trace.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
class HttpProfanityFilterClient implements ProfanityFilterClient {

    private final ProfanityProperties properties;
    private final RestClient restClient;

    HttpProfanityFilterClient(ProfanityProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public ProfanityCheckResponse check(List<String> texts) {
        String checkUrl = properties.getCheckUrl();
        if (!StringUtils.hasText(checkUrl)) {
            throw new ProfanityFilterException("PROFANITY_FILTER_BASE_URL is not configured.");
        }

        try {
            return restClient.post()
                    .uri(checkUrl)
                    .body(new ProfanityCheckRequest(texts))
                    .retrieve()
                    .body(ProfanityCheckResponse.class);
        } catch (RestClientResponseException exception) {
            throw new ProfanityFilterException(
                    "Profanity API returned HTTP status " + exception.getStatusCode().value() + ".",
                    exception
            );
        } catch (RestClientException exception) {
            throw new ProfanityFilterException("Profanity API request failed.", exception);
        }
    }

    private record ProfanityCheckRequest(List<String> texts) {
    }
}
