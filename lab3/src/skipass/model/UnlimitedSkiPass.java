package skipass.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Ski-pass на N днів або сезонний: у межах терміну дії - без обмежень.
public class UnlimitedSkiPass extends SkiPass {

    public UnlimitedSkiPass(String id, SkiPassType type, LocalDate validFrom, LocalDate validUntil) {
        super(id, type, validFrom, validUntil);
        if (type.kind() != PassKind.DAYS && type.kind() != PassKind.SEASON) {
            throw new IllegalArgumentException("Тип " + type + " не підходить для UnlimitedSkiPass");
        }
    }

    @Override
    protected DenyReason checkSpecific(LocalDateTime now) {
        return null;
    }
}
