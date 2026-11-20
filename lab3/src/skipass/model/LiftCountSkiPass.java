package skipass.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Ski-pass на певну кількість підйомів (діє до кінця сезону).
public class LiftCountSkiPass extends SkiPass {

    private int liftsLeft;

    public LiftCountSkiPass(String id, SkiPassType type, LocalDate validFrom, LocalDate validUntil) {
        this(id, type, validFrom, validUntil, type.amount());
    }

    // Конструктор для "зчитування" картки з уже частково використаними підйомами.
    public LiftCountSkiPass(String id, SkiPassType type, LocalDate validFrom, LocalDate validUntil, int liftsLeft) {
        super(id, type, validFrom, validUntil);
        if (type.kind() != PassKind.LIFTS) {
            throw new IllegalArgumentException("Тип " + type + " не підходить для LiftCountSkiPass");
        }
        if (liftsLeft < 0 || liftsLeft > type.amount()) {
            throw new IllegalArgumentException("Некоректна кількість підйомів: " + liftsLeft);
        }
        this.liftsLeft = liftsLeft;
    }

    @Override
    protected DenyReason checkSpecific(LocalDateTime now) {
        return liftsLeft > 0 ? null : DenyReason.NO_LIFTS_LEFT;
    }

    @Override
    public void registerRide() {
        if (liftsLeft <= 0) {
            throw new IllegalStateException("На картці " + getId() + " немає підйомів");
        }
        liftsLeft--;
    }

    public int getLiftsLeft() {
        return liftsLeft;
    }

    @Override
    public String describe() {
        return super.describe() + ", підйомів: " + liftsLeft + "/" + getType().amount();
    }
}
