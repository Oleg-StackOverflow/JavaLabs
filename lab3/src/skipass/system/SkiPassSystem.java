package skipass.system;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import skipass.model.*;

/*
 Система, в якій ведеться реєстр виданих карток.
 Вміє: випустити ski-pass, заблокувати ski-pass, відповісти турнікету,
 чи картка зареєстрована і чи не заблокована.
 */
public class SkiPassSystem {

    private final Clock clock;
    private final Season season;
    private final Map<String, SkiPass> issued = new LinkedHashMap<>();
    private final Map<String, String> blocked = new HashMap<>(); // id -> причина
    private int nextNumber = 1;

    public SkiPassSystem(Clock clock, Season season) {
        this.clock = Objects.requireNonNull(clock);
        this.season = Objects.requireNonNull(season);
    }

    // Випуск картки, що починає діяти сьогодні (або з найближчого підходящого дня)
    public Card issue(SkiPassType type) {
        return issue(type, LocalDate.now(clock));
    }

    public synchronized Card issue(SkiPassType type, LocalDate wantedStart) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(wantedStart);
        if (wantedStart.isAfter(season.end())) {
            throw new IllegalStateException("Сезон уже закінчився (" + season.end() + ")");
        }
        LocalDate start = wantedStart.isBefore(season.start()) ? season.start() : wantedStart;
        String id = String.format("SP-%06d", nextNumber++);

        SkiPass pass = switch (type.kind()) {
            case HALF_DAY -> new HalfDaySkiPass(id, type, firstMatchingDay(type, start));
            case DAYS -> {
                LocalDate from = firstMatchingDay(type, start);
                yield new UnlimitedSkiPass(id, type, from, nthMatchingDay(type, from, type.amount()));
            }
            case LIFTS -> new LiftCountSkiPass(id, type, start, season.end());
            case SEASON -> new UnlimitedSkiPass(id, type, season.start(), season.end());
        };
        issued.put(id, pass);
        return new Card(pass);
    }

    // Блокування за порушення правил підйому.
    public synchronized void block(String id, String reason) {
        if (!issued.containsKey(id)) {
            throw new NoSuchElementException("Картку " + id + " не знайдено");
        }
        blocked.put(id, reason == null ? "без причини" : reason);
    }

    public synchronized boolean isRegistered(String id) {
        return issued.containsKey(id);
    }

    public synchronized boolean isBlocked(String id) {
        return blocked.containsKey(id);
    }

    public synchronized String blockReason(String id) {
        return blocked.get(id);
    }

    public synchronized List<SkiPass> allPasses() {
        return List.copyOf(issued.values());
    }

    public synchronized Optional<SkiPass> find(String id) {
        return Optional.ofNullable(issued.get(id));
    }

    public Season getSeason() {
        return season;
    }

    // Workday-картку, куплену в суботу, починаємо з понеділка, і навпаки.
    private LocalDate firstMatchingDay(SkiPassType type, LocalDate from) {
        LocalDate d = from;
        while (!type.days().matches(d)) {
            d = d.plusDays(1);
        }
        if (d.isAfter(season.end())) {
            throw new IllegalStateException("До кінця сезону немає жодного підходящого дня");
        }
        return d;
    }

    // n-й підходящий день, рахуючи from як перший. "2 дні" на вихідні = субота + неділя,
    // "5 днів" на будні = 5 робочих днів поспіль (вихідні між ними не рахуються).
    private LocalDate nthMatchingDay(SkiPassType type, LocalDate from, int n) {
        LocalDate d = from;
        int counted = 1;
        while (counted < n) {
            d = d.plusDays(1);
            if (type.days().matches(d)) {
                counted++;
            }
        }
        return d.isAfter(season.end()) ? season.end() : d;
    }
}
