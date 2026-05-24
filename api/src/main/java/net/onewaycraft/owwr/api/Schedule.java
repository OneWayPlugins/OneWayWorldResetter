package net.onewaycraft.owwr.api;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * @brief Cronograma de reset com cálculo determinístico do próximo disparo.
 *
 * Modos: DAILY, WEEKLY (com DayOfWeek), MONTHLY (dia 1-31, clamp ao final do mês),
 * CRON (futuro — fase 9 do plano, usa cron-utils).
 */
public final class Schedule {

    private final ResetType type;
    private final LocalTime time;
    private final DayOfWeek weekday;   // weekly only
    private final int dayOfMonth;      // monthly only (1-31)
    private final String cronExpression; // cron only

    private Schedule(ResetType t, LocalTime time, DayOfWeek dow, int dom, String cron) {
        this.type = t;
        this.time = time;
        this.weekday = dow;
        this.dayOfMonth = dom;
        this.cronExpression = cron;
    }

    public static Schedule daily(LocalTime time) {
        Objects.requireNonNull(time, "time");
        return new Schedule(ResetType.DAILY, time, null, 0, null);
    }

    public static Schedule weekly(DayOfWeek dow, LocalTime time) {
        Objects.requireNonNull(dow, "dow");
        Objects.requireNonNull(time, "time");
        return new Schedule(ResetType.WEEKLY, time, dow, 0, null);
    }

    public static Schedule monthly(int dayOfMonth, LocalTime time) {
        Objects.requireNonNull(time, "time");
        if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException("dayOfMonth");
        return new Schedule(ResetType.MONTHLY, time, null, dayOfMonth, null);
    }

    public static Schedule cron(String expression) {
        return new Schedule(ResetType.CRON, null, null, 0, Objects.requireNonNull(expression));
    }

    public ResetType type() { return type; }

    /**
     * @brief Próximo Instant em que este Schedule dispara, estritamente após {@code now}.
     * @throws UnsupportedOperationException para CRON (implementado na Fase 9).
     */
    public Instant nextReset(Instant now, ZoneId zone) {
        ZonedDateTime z = now.atZone(zone);
        return switch (type) {
            case DAILY -> nextDaily(z);
            case WEEKLY -> nextWeekly(z);
            case MONTHLY -> nextMonthly(z);
            case CRON -> throw new UnsupportedOperationException("cron not implemented");
        };
    }

    private Instant nextDaily(ZonedDateTime z) {
        ZonedDateTime candidate = z.toLocalDate().atTime(time).atZone(z.getZone());
        if (!candidate.isAfter(z)) candidate = candidate.plusDays(1);
        return candidate.toInstant();
    }

    private Instant nextWeekly(ZonedDateTime z) {
        ZonedDateTime candidate = z.with(TemporalAdjusters.nextOrSame(weekday))
            .toLocalDate().atTime(time).atZone(z.getZone());
        if (!candidate.isAfter(z)) candidate = candidate.plusWeeks(1);
        return candidate.toInstant();
    }

    private Instant nextMonthly(ZonedDateTime z) {
        ZonedDateTime candidate = buildMonthlyCandidate(z.getYear(), z.getMonthValue(), z.getZone());
        if (!candidate.isAfter(z)) {
            YearMonth next = YearMonth.from(z).plusMonths(1);
            candidate = buildMonthlyCandidate(next.getYear(), next.getMonthValue(), z.getZone());
        }
        return candidate.toInstant();
    }

    private ZonedDateTime buildMonthlyCandidate(int year, int month, ZoneId zone) {
        YearMonth ym = YearMonth.of(year, month);
        int effectiveDay = Math.min(dayOfMonth, ym.lengthOfMonth());
        return LocalDate.of(year, month, effectiveDay).atTime(time).atZone(zone);
    }
}
