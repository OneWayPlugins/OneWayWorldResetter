package net.onewaycraft.owwr.api;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void dailyScheduleAtFiveAmReturnsNextFiveAm() {
        Instant now = ZonedDateTime.of(2026, 5, 24, 3, 0, 0, 0, UTC).toInstant();
        Schedule s = Schedule.daily(LocalTime.of(5, 0));
        assertThat(s.nextReset(now, UTC))
            .isEqualTo(ZonedDateTime.of(2026, 5, 24, 5, 0, 0, 0, UTC).toInstant());
    }

    @Test
    void dailyScheduleSkipsToTomorrowWhenPastTime() {
        Instant now = ZonedDateTime.of(2026, 5, 24, 6, 0, 0, 0, UTC).toInstant();
        Schedule s = Schedule.daily(LocalTime.of(5, 0));
        assertThat(s.nextReset(now, UTC))
            .isEqualTo(ZonedDateTime.of(2026, 5, 25, 5, 0, 0, 0, UTC).toInstant());
    }

    @Test
    void weeklyScheduleReturnsNextOccurrenceOfWeekday() {
        // 2026-05-24 is a Sunday. Target Monday at 05:00.
        Instant now = ZonedDateTime.of(2026, 5, 24, 12, 0, 0, 0, UTC).toInstant();
        Schedule s = Schedule.weekly(DayOfWeek.MONDAY, LocalTime.of(5, 0));
        assertThat(s.nextReset(now, UTC))
            .isEqualTo(ZonedDateTime.of(2026, 5, 25, 5, 0, 0, 0, UTC).toInstant());
    }

    @Test
    void monthlyScheduleClampsDayToEndOfMonth() {
        // Day 31 in a 30-day month → fires on day 30.
        Instant now = ZonedDateTime.of(2026, 6, 15, 0, 0, 0, 0, UTC).toInstant();
        Schedule s = Schedule.monthly(31, LocalTime.of(5, 0));
        assertThat(s.nextReset(now, UTC))
            .isEqualTo(ZonedDateTime.of(2026, 6, 30, 5, 0, 0, 0, UTC).toInstant());
    }

    @Test
    void monthlyScheduleRollsToNextMonthWhenPastTargetDay() {
        Instant now = ZonedDateTime.of(2026, 5, 25, 12, 0, 0, 0, UTC).toInstant();
        Schedule s = Schedule.monthly(1, LocalTime.of(5, 0));
        assertThat(s.nextReset(now, UTC))
            .isEqualTo(ZonedDateTime.of(2026, 6, 1, 5, 0, 0, 0, UTC).toInstant());
    }
}
