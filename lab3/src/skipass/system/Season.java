package skipass.system;

import java.time.LocalDate;
import java.util.Objects;

// Межі гірськолижного сезону
public record Season(LocalDate start, LocalDate end) {

    public Season {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Кінець сезону раніше початку");
        }
    }

    public boolean contains(LocalDate d) {
        return !d.isBefore(start) && !d.isAfter(end);
    }
}
