package skipass.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/*
 Дані, які зберігаються на самій картці: id, тип, термін дії (+ залишок підйомів у підкласі).
 Перевірка зроблена через шаблонний метод: check() робить загальні перевірки
 (дата, день тижня, години роботи), а специфічну частину робить checkSpecific() у підкласах.
 */
public abstract class SkiPass {

    // Години роботи підйомника - спільні для всіх карток.
    public static final LocalTime LIFT_OPENS = LocalTime.of(9, 0);
    public static final LocalTime LIFT_CLOSES = LocalTime.of(17, 0);

    private final String id;
    private final SkiPassType type;
    private final LocalDate validFrom;
    private final LocalDate validUntil;

    protected SkiPass(String id, SkiPassType type, LocalDate validFrom, LocalDate validUntil) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.validFrom = Objects.requireNonNull(validFrom);
        this.validUntil = Objects.requireNonNull(validUntil);
        if (validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("validUntil < validFrom");
        }
    }

    // @return null, якщо прохід можна дозволити, інакше причина відмови
    public final DenyReason check(LocalDateTime now) {
        LocalDate date = now.toLocalDate();
        if (date.isBefore(validFrom)) {
            return DenyReason.NOT_YET_VALID;
        }
        if (date.isAfter(validUntil)) {
            return DenyReason.EXPIRED;
        }
        if (!type.days().matches(date)) {
            return DenyReason.WRONG_DAY;
        }
        LocalTime t = now.toLocalTime();
        if (t.isBefore(LIFT_OPENS) || !t.isBefore(LIFT_CLOSES)) {
            return DenyReason.LIFT_CLOSED;
        }
        return checkSpecific(now);
    }

    // Додаткова перевірка конкретного виду картки.
    protected abstract DenyReason checkSpecific(LocalDateTime now);

    // Викликається турнікетом після успішної перевірки. За замовчуванням нічого не списує.
    public void registerRide() {
        // картки без обліку підйомів нічого не списують
    }

    public String getId() { return id; }
    public SkiPassType getType() { return type; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidUntil() { return validUntil; }

    // Текст для виводу на табло турнікету.
    public String describe() {
        return String.format("%s [%s] %s..%s", id, type.title(), validFrom, validUntil);
    }

    @Override
    public String toString() {
        return describe();
    }
}
