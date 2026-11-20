package skipass.app;

import java.time.*;

/*
 Годинник, який можна переводити вручну. Потрібен, щоб у консолі і тестах
 "прожити" кілька днів сезону, не чекаючи реального часу.
 */
public class SimulatedClock extends Clock {

    private LocalDateTime now;
    private final ZoneId zone;

    public SimulatedClock(LocalDateTime start) {
        this(start, ZoneId.of("Europe/Kyiv"));
    }

    private SimulatedClock(LocalDateTime start, ZoneId zone) {
        this.now = start;
        this.zone = zone;
    }

    public void set(LocalDateTime t) {
        this.now = t;
    }

    public void plus(Duration d) {
        this.now = now.plus(d);
    }

    public LocalDateTime now() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId z) {
        return new SimulatedClock(now, z);
    }

    @Override
    public Instant instant() {
        return now.atZone(zone).toInstant();
    }
}
