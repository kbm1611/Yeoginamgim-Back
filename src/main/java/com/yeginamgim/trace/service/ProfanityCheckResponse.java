package com.yeginamgim.trace.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record ProfanityCheckResponse(
        boolean blocked,
        List<Result> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Result(
            @JsonProperty("isProfanity") boolean isProfanity,
            double score
    ) {
    }
}
