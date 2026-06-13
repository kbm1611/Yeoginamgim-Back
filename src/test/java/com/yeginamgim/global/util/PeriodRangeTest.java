package com.yeginamgim.global.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodRangeTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Test
    void startAtReturnsInstantForKoreanCalendarBoundaries() {
        LocalDate today = LocalDate.now(SEOUL_ZONE);

        Instant todayStart = PeriodRange.startAt("today");
        Instant weekStart = PeriodRange.startAt("week");
        Instant monthStart = PeriodRange.startAt("month");
        Instant yearStart = PeriodRange.startAt("year");

        assertThat(todayStart).isEqualTo(today.atStartOfDay(SEOUL_ZONE).toInstant());
        assertThat(weekStart).isEqualTo(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(SEOUL_ZONE)
                .toInstant());
        assertThat(monthStart).isEqualTo(today.withDayOfMonth(1).atStartOfDay(SEOUL_ZONE).toInstant());
        assertThat(yearStart).isEqualTo(today.withDayOfYear(1).atStartOfDay(SEOUL_ZONE).toInstant());
    }
}
