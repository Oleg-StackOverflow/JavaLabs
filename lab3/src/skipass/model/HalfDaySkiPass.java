package skipass.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Ski-pass на півдня: діє в один день і тільки у своєму проміжку часу.
public class HalfDaySkiPass extends SkiPass {

    public HalfDaySkiPass(String id, SkiPassType type, LocalDate day) {
        super(id, type, day, day);
        if (type.kind() != PassKind.HALF_DAY) {
            throw new IllegalArgumentException("Тип " + type + " не підходить для HalfDaySkiPass");
        }
    }

    @Override
    protected DenyReason checkSpecific(LocalDateTime now) {
        return getType().halfDay().contains(now.toLocalTime()) ? null : DenyReason.OUTSIDE_HALF_DAY;
    }

    @Override
    public String describe() {
        return super.describe() + " " + getType().halfDay();
    }
}
