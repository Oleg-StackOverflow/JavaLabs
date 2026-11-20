package skipass.turnstile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import skipass.model.*;
import skipass.system.SkiPassSystem;

/*
 Вбудований процесор турнікету. Порядок перевірки:
 зчитування -> є в реєстрі -> не заблокована -> перевірка самої картки
 (дати, день тижня, години, залишок підйомів) -> списання підйому -> прохід.
 */
public class Turnstile {

    private final String name;
    private final SkiPassSystem system;
    private final Clock clock;
    private final TurnstileStats stats = new TurnstileStats();

    public Turnstile(String name, SkiPassSystem system, Clock clock) {
        this.name = name;
        this.system = Objects.requireNonNull(system);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized AccessResult pass(Card card) {
        AccessResult result = check(card);
        stats.record(result);
        return result;
    }

    private AccessResult check(Card card) {
        SkiPass pass;
        try {
            pass = card.read();
        } catch (CardReadException e) {
            return AccessResult.deny(DenyReason.UNREADABLE, null, null);
        }
        String id = pass.getId();
        SkiPassType type = pass.getType();

        if (!system.isRegistered(id)) {
            return AccessResult.deny(DenyReason.UNKNOWN_CARD, id, type);
        }
        if (system.isBlocked(id)) {
            return AccessResult.deny(DenyReason.BLOCKED, id, type);
        }
        DenyReason reason = pass.check(LocalDateTime.now(clock));
        if (reason != null) {
            return AccessResult.deny(reason, id, type);
        }

        pass.registerRide(); // знімаємо підйом лише після всіх перевірок
        String details = (pass instanceof LiftCountSkiPass l)
                ? "залишилось підйомів: " + l.getLiftsLeft()
                : "діє до " + pass.getValidUntil();
        return AccessResult.allow(id, type, details);
    }

    public TurnstileStats getStats() {
        return stats;
    }

    public String getName() {
        return name;
    }
}
