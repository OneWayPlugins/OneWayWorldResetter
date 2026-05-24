package net.onewaycraft.owwr.core.schedule;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import net.onewaycraft.owwr.api.ResetType;
import net.onewaycraft.owwr.api.Schedule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @brief Avalia nextReset para schedules CRON (api Schedule não conhece cron-utils).
 *
 * Para tipos não-CRON, delega ao Schedule.nextReset.
 */
public final class CronEvaluator {

    private static final CronParser PARSER =
        new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    private CronEvaluator() {}

    public static Instant nextReset(Schedule schedule, Instant now, ZoneId zone) {
        if (schedule.type() != ResetType.CRON) {
            return schedule.nextReset(now, zone);
        }
        String expr = schedule.cronExpression();
        if (expr == null) throw new IllegalStateException("CRON schedule has no expression");
        Cron cron = PARSER.parse(expr);
        ExecutionTime exec = ExecutionTime.forCron(cron);
        ZonedDateTime zonedNow = now.atZone(zone);
        return exec.nextExecution(zonedNow)
            .orElseThrow(() -> new IllegalStateException("cron has no next execution: " + expr))
            .toInstant();
    }
}
