package skipass.turnstile;

import skipass.model.DenyReason;
import skipass.model.SkiPassType;

/*
 Результат однієї спроби проходу.
 type == null, якщо картку не вдалося зчитати.
 */
public record AccessResult(boolean allowed, DenyReason reason, String passId, SkiPassType type, String details) {

    static AccessResult allow(String id, SkiPassType type, String details) {
        return new AccessResult(true, null, id, type, details);
    }

    static AccessResult deny(DenyReason reason, String id, SkiPassType type) {
        return new AccessResult(false, reason, id, type, reason.message());
    }

    @Override
    public String toString() {
        String who = passId == null ? "?" : passId;
        return allowed
                ? "ПРОХІД ДОЗВОЛЕНО  " + who + (details == null ? "" : " (" + details + ")")
                : "ПРОХІД ЗАБОРОНЕНО " + who + ": " + details;
    }
}
