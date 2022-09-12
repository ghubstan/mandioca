package mandioca.bitcoin.function;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.*;
import static mandioca.bitcoin.function.BigIntegerFunctions.formatInt;
import static mandioca.bitcoin.function.LongFunctions.formatLong;

@SuppressWarnings("unused")
public class TimeFunctions {

    private static final MathContext MC = new MathContext(4, RoundingMode.HALF_UP);

    // Day to seconds funcs:  dd, dd:hh
    public static final Function<Long, Integer> daysToSeconds = (d) -> Math.toIntExact(DAYS.toSeconds(d));
    public static final BiFunction<Long, Integer, Integer> daysHoursToSeconds = (d, h) -> Math.toIntExact(DAYS.toSeconds(d) + HOURS.toSeconds(h));

    // Day to millis funcs:  dd, dd:hh, dd:hh:mm, dd:hh:mm:ss, dd:hh:mm:ss:mmm
    public static final Function<Long, Long> daysToMilliseconds = DAYS::toMillis;
    public static final BiFunction<Long, Integer, Long> daysHoursToMilliseconds = (d, h) -> DAYS.toMillis(d) + HOURS.toMillis(h);
    public static final TriFunction<Long, Integer, Integer, Long> daysHoursMinutesToMilliseconds = (d, h, m) ->
            DAYS.toMillis(d) + HOURS.toMillis(h) + MINUTES.toMillis(m);
    public static final QuadriFunction<Long, Integer, Integer, Integer, Long> daysHoursMinutesSecondsToMilliseconds = (d, h, m, s) ->
            DAYS.toMillis(d) + HOURS.toMillis(h) + MINUTES.toMillis(m) + SECONDS.toMillis(s);
    public static final QuinqueFunction<Long, Integer, Integer, Integer, Long, Long> daysHoursMinutesSecondsMillisToMilliseconds = (d, h, m, s, millis) ->
            DAYS.toMillis(d) + HOURS.toMillis(h) + MINUTES.toMillis(m) + SECONDS.toMillis(s) + millis;

    // Hour to millis funcs:  hh, hh:mm, hh:mm:ss, hh:mm:ss:mmm
    public static final Function<Integer, Long> hoursToMilliseconds = HOURS::toMillis;
    public static final BiFunction<Integer, Integer, Long> hoursMinutesToMilliseconds = (h, m) -> HOURS.toMillis(h) + MINUTES.toMillis(m);
    public static final TriFunction<Integer, Integer, Integer, Long> hoursMinutesSecondsToMilliseconds = (h, m, s) ->
            HOURS.toMillis(h) + MINUTES.toMillis(m) + SECONDS.toMillis(s);
    public static final QuadriFunction<Integer, Integer, Integer, Long, Long> hoursMinutesSecondsMillisToMilliseconds = (h, m, s, millis) ->
            HOURS.toMillis(h) + MINUTES.toMillis(m) + SECONDS.toMillis(s) + millis;

    // Minute to millis funcs:  mm, mm:ss, mm:ss:mmm
    public static final Function<Integer, Long> minutesToMilliseconds = MINUTES::toMillis;
    public static final BiFunction<Integer, Integer, Long> minutesSecondsToMilliseconds = (m, s) -> MINUTES.toMillis(m) + SECONDS.toMillis(s);
    public static final TriFunction<Integer, Integer, Long, Long> minutesSecondsMillisToMilliseconds = (m, s, millis) ->
            MINUTES.toMillis(m) + SECONDS.toMillis(s) + millis;

    // Second to millis funcs:  ss, ss:mmm
    public static final Function<Integer, Long> secondsToMilliseconds = SECONDS::toMillis;
    public static final BiFunction<Integer, Integer, Long> secondsMillisToMilliseconds = (s, m) -> SECONDS.toMillis(s) + m;


    public static final Function<Long, Double> millisecondsToSecondsAsDouble = (millis) -> {
        Duration duration = Duration.ofMillis(millis);
        String secondsPart = String.valueOf(duration.toSecondsPart());
        String millisPart = String.valueOf(duration.toMillisPart());
        return Double.parseDouble(secondsPart + "." + millisPart);
    };

    public static final Function<Long, Double> secondsToMinutesAsDouble = (s) -> {
        Duration duration = Duration.ofSeconds(s);
        String secondsPart = String.valueOf(duration.toSecondsPart());
        String millisPart = String.valueOf(duration.toMillisPart());
        return Double.parseDouble(secondsPart + "." + millisPart);
    };

    private static final BiFunction<String, Integer, String> durationPart = (part, n) -> {
        if (n == 0) {
            return "";
        } else {
            return n == 1 ? n + " " + part + " " : n + " " + part + "s ";
        }
    };
    private static final Function<Duration, String> durationParts = (d) -> {
        if (d.isZero()) {
            return "0 milliseconds";
        }
        String durationString = durationPart.apply("day", (int) d.toDaysPart()) +
                durationPart.apply("hour", d.toHoursPart()) +
                durationPart.apply("minute", d.toMinutesPart()) +
                durationPart.apply("second", d.toSecondsPart()) +
                (d.toMillisPart() > 0 ? d.toMillisPart() + " milliseconds" : "");
        return durationString.trim();
    };
    public static final Function<Long, String> durationString = (millis) -> durationParts.apply(Duration.ofMillis(millis));

    public static final BiFunction<Long, Long, Integer> eventsPerSecond = (events, seconds) ->
            new BigDecimal(events / seconds, MC).intValue();

    public static TriConsumer<Logger, String, Long> logTime = (log, description, millis) ->
            log.info("{}:  {} ms ({})", description, formatLong.apply(millis), durationString.apply(millis));

    public static QuadriConsumer<Logger, String, Long, Long> logTimeAndRequestRate = (log, description, totalReqs, millis) -> {
        long seconds = MILLISECONDS.toSeconds(millis);
        log.info("{} for {} requests:  {} ms ({}) @ rate of {} reqs/sec",
                description,
                formatLong.apply(totalReqs), formatLong.apply(millis), durationString.apply(millis),
                formatInt.apply(eventsPerSecond.apply(totalReqs, seconds)));
    };


    public static final int EIGHT_WEEKS_AS_SECONDS = daysToSeconds.apply((long) 8 * 7);
    public static final int TWO_WEEKS_AS_SECONDS = daysToSeconds.apply((long) 2 * 7);
    public static final int THREE_AND_A_HALF_DAYS_AS_SECONDS = daysHoursToSeconds.apply((long) 3, 12);
    public static final int TEN_MINUTES_AS_SECONDS = (int) MINUTES.toSeconds(10);


    public static void main(String[] args) {
        long millis = minutesToMilliseconds.apply(16);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = minutesToMilliseconds.apply(30);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = minutesSecondsToMilliseconds.apply(30, 22);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = minutesSecondsMillisToMilliseconds.apply(30, 22, 22L);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = hoursToMilliseconds.apply(1);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));


        millis = hoursMinutesSecondsToMilliseconds.apply(1, 6, 6);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = hoursMinutesSecondsMillisToMilliseconds.apply(1, 6, 6, 66L);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));


        millis = hoursMinutesSecondsMillisToMilliseconds.apply(1, 0, 3, 175L);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        // 14 hrs 6 mins 6 secs 66 millis
        millis = hoursMinutesSecondsMillisToMilliseconds.apply(14, 6, 6, 66L);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        // 3 days 6 mins 6 secs 66 millis
        millis = daysHoursMinutesSecondsMillisToMilliseconds.apply(3L, 0, 6, 6, 66L);
        out.println(String.format("%s = %s milliseconds", durationString.apply(millis), formatLong.apply(millis)));

        millis = 3_604_000L;
        long seconds = MILLISECONDS.toSeconds(millis);
        long totalReqs = 6_000_000L;
        out.println(String.format("%s = %s milliseconds, %s events @ rate of %s events / second",
                formatLong.apply(millis), durationString.apply(millis),
                formatLong.apply(totalReqs), formatInt.apply(eventsPerSecond.apply(totalReqs, seconds))));

    }
}
