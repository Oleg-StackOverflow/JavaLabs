package skipass.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

// У які дні тижня діє ski-pass.
public enum DayCategory {
    WORKDAY("робочі дні"),
    WEEKEND("вихідні дні"),
    ANY("будь-які дні");

    private final String title;

    DayCategory(String title) {
        this.title = title;
    }

    public boolean matches(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        boolean weekend = d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
        return switch (this) {
            case WORKDAY -> !weekend;
            case WEEKEND -> weekend;
            case ANY -> true;
        };
    }

    public String title() {
        return title;
    }
}
