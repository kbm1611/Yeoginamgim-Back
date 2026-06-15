package com.yeginamgim.global.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public final class PeriodRange {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private PeriodRange() {
    }

    public static Instant startAt(String period) {
        String normalizedPeriod = period == null || period.isBlank() ? "today" : period.trim().toLowerCase();
        LocalDate today = LocalDate.now(SEOUL_ZONE);

        return switch (normalizedPeriod) {
            case "today" -> today.atStartOfDay(SEOUL_ZONE).toInstant();
            case "week" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(SEOUL_ZONE)
                    .toInstant();
            case "month" -> today.withDayOfMonth(1).atStartOfDay(SEOUL_ZONE).toInstant();
            case "year" -> today.withDayOfYear(1).atStartOfDay(SEOUL_ZONE).toInstant();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "period는 today, week, month, year 중 하나여야 합니다.");
        };
    }
}
