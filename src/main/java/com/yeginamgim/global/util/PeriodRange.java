package com.yeginamgim.global.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public final class PeriodRange {

    private PeriodRange() {
    }

    public static LocalDateTime startAt(String period) {
        String normalizedPeriod = period == null || period.isBlank() ? "today" : period.trim().toLowerCase();
        LocalDate today = LocalDate.now();

        return switch (normalizedPeriod) {
            case "today" -> today.atStartOfDay();
            case "week" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case "month" -> today.withDayOfMonth(1).atStartOfDay();
            case "year" -> today.withDayOfYear(1).atStartOfDay();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "period는 today, week, month, year 중 하나여야 합니다.");
        };
    }
}
