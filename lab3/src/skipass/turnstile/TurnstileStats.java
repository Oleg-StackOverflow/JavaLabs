package skipass.turnstile;

import java.util.EnumMap;
import java.util.Map;

import skipass.model.DenyReason;
import skipass.model.SkiPassType;

// Облік дозволів і відмов: загальний, по типах ski-pass і по причинах відмови.
public class TurnstileStats {

    // Лічильник для одного рядка звіту.
    public static final class Counter {
        private int allowed;
        private int denied;

        public int allowed() { return allowed; }
        public int denied() { return denied; }
        public int total() { return allowed + denied; }
    }

    private final Counter total = new Counter();
    private final Map<SkiPassType, Counter> byType = new EnumMap<>(SkiPassType.class);
    private final Counter unreadable = new Counter(); // тип невідомий - окремий рядок
    private final Map<DenyReason, Integer> byReason = new EnumMap<>(DenyReason.class);

    synchronized void record(AccessResult r) {
        Counter forType = (r.type() == null) ? unreadable : byType.computeIfAbsent(r.type(), t -> new Counter());
        if (r.allowed()) {
            total.allowed++;
            forType.allowed++;
        } else {
            total.denied++;
            forType.denied++;
            byReason.merge(r.reason(), 1, Integer::sum);
        }
    }

    public synchronized int allowed() { return total.allowed; }
    public synchronized int denied() { return total.denied; }

    public synchronized Counter forType(SkiPassType type) {
        Counter c = byType.get(type);
        return c == null ? new Counter() : c;
    }

    public synchronized int deniedFor(DenyReason reason) {
        return byReason.getOrDefault(reason, 0);
    }

    // 1) Сумарні дані.
    public synchronized String summaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Всього спроб: %d, дозволено: %d, відмовлено: %d%n",
                total.total(), total.allowed, total.denied));
        if (!byReason.isEmpty()) {
            sb.append("Причини відмов:\n");
            byReason.forEach((reason, n) -> sb.append(String.format("  %-16s %3d  (%s)%n", reason, n, reason.message())));
        }
        return sb.toString();
    }

    // 2) Дані, розбиті по типах ski-pass.
    public synchronized String byTypeReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-26s %9s %9s%n", "Тип ski-pass", "дозволено", "відмова"));
        for (SkiPassType t : SkiPassType.values()) {        // у порядку з enum, а не випадковому
            Counter c = byType.get(t);
            if (c != null) {
                sb.append(String.format("%-26s %9d %9d%n", t.title(), c.allowed, c.denied));
            }
        }
        if (unreadable.total() > 0) {
            sb.append(String.format("%-26s %9d %9d%n", "(не зчитано)", unreadable.allowed, unreadable.denied));
        }
        return sb.toString();
    }
}
