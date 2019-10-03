package com.scottlogic.deg.common.profile.constraintdetail;

import com.scottlogic.deg.common.date.TemporalAdjusterGenerator;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.function.IntFunction;

public class ChronoUnitGranularity implements Granularity<OffsetDateTime> {

    private final ChronoUnit chronoUnit;
    private final boolean workingDay;
    private final TemporalAdjusterGenerator temporalAdjusterGenerator;

    public ChronoUnitGranularity(ChronoUnit chronoUnit){
        this(chronoUnit, false);
    }
    public ChronoUnitGranularity(ChronoUnit chronoUnit, boolean workingDay) {
        this.chronoUnit = chronoUnit;
        this.workingDay = workingDay;
        this.temporalAdjusterGenerator = new TemporalAdjusterGenerator(chronoUnit, workingDay);
    }

    @Override
    public boolean isCorrectScale(OffsetDateTime value) {
        return value.equals(trimToGranularity(value));
    }

    @Override
    public Granularity<OffsetDateTime> merge(Granularity<OffsetDateTime> otherGranularity) {
        ChronoUnitGranularity other = (ChronoUnitGranularity) otherGranularity; //TODO deal with working days, also test
        return chronoUnit.compareTo(other.chronoUnit) <= 0 ? other : this;
    }

    @Override
    public OffsetDateTime getNext(OffsetDateTime value, int amount){
        return OffsetDateTime.from(temporalAdjusterGenerator.adjuster(amount).adjustInto(value));
    }

    @Override
    public OffsetDateTime trimToGranularity(OffsetDateTime d) {
        // is there a generic way of doing this with chronounit?
        switch (chronoUnit) {
            case MILLIS:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond(), nanoToMilli(d.getNano()), ZoneOffset.UTC);
            case SECONDS:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond(), 0, ZoneOffset.UTC);
            case MINUTES:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), 0, 0, ZoneOffset.UTC);
            case HOURS:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), 0, 0, 0, ZoneOffset.UTC);
            case DAYS:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.UTC);
            case MONTHS:
                return OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC);
            case YEARS:
                return OffsetDateTime.of(d.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            default:
                throw new UnsupportedOperationException(chronoUnit + "not yet supported as a granularity");
        }
    }

    private static int nanoToMilli(int nano) {
        int factor = NANOS_IN_MILLIS;
        return (nano / factor) * factor;
    }
    private static final int NANOS_IN_MILLIS = 1_000_000;

}
