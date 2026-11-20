package skipass.model;

import java.time.LocalTime;

// Половина дня для ski-pass "на півдня".
public enum HalfDay {
    MORNING(LocalTime.of(9, 0), LocalTime.of(13, 0)),
    AFTERNOON(LocalTime.of(13, 0), LocalTime.of(17, 0));

    private final LocalTime from;
    private final LocalTime to;

    HalfDay(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;
    }

    // [from, to) - о 13:00 ранковий вже не діє, а обідній вже діє
    public boolean contains(LocalTime t) {
        return !t.isBefore(from) && t.isBefore(to);
    }

    @Override
    public String toString() {
        return from + "-" + to;
    }
}
