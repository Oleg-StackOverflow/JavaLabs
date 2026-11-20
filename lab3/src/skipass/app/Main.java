package skipass.app;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import skipass.model.Card;
import skipass.model.SkiPass;
import skipass.model.SkiPassType;
import skipass.model.UnlimitedSkiPass;
import skipass.system.Season;
import skipass.system.SkiPassSystem;
import skipass.turnstile.AccessResult;
import skipass.turnstile.Turnstile;

/*
 Консольна модель турнікету лижного підйомника.
 Запуск без аргументів - інтерактивне меню; з аргументом --demo - готовий сценарій.
 */
public class Main {

    private static final Locale UA = Locale.forLanguageTag("uk");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Season SEASON = new Season(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 3, 31));

    private final SimulatedClock clock;
    private final SkiPassSystem system;
    private final Turnstile turnstile;
    // "кишеня лижника": видані картки, які можна прикласти до турнікету
    private final Map<String, Card> cards = new LinkedHashMap<>();

    Main(LocalDateTime start) {
        clock = new SimulatedClock(start);
        system = new SkiPassSystem(clock, SEASON);
        turnstile = new Turnstile("Підйомник №1", system, clock);
    }

    public static void main(String[] args) {
        Main app = new Main(LocalDateTime.of(2026, 12, 14, 9, 30)); // понеділок
        if (args.length > 0 && args[0].equals("--demo")) {
            app.demo();
        } else {
            app.menu(new Scanner(System.in));
        }
    }

    //  меню

    private void menu(Scanner in) {
        System.out.println("Турнікет лижного підйомника. Сезон: " + SEASON.start() + " - " + SEASON.end());
        while (true) {
            System.out.println();
            System.out.println("Зараз: " + clock.now().format(FMT) + " (" + clock.now().getDayOfWeek().getDisplayName(TextStyle.FULL, UA) + ")");
            System.out.println("1 - випустити ski-pass        2 - заблокувати ski-pass");
            System.out.println("3 - прикласти картку          4 - пошкодити картку");
            System.out.println("5 - змінити дату/час          6 - список карток");
            System.out.println("7 - звіт: сумарно             8 - звіт: по типах");
            System.out.println("0 - вихід");
            String choice = ask(in, "> ");
            if (choice == null) return; // кінець вводу
            switch (choice) {
                case "1" -> issueMenu(in);
                case "2" -> {
                    String id = askCardId(in);
                    if (id != null) {
                        system.block(id, ask(in, "Причина: "));
                        System.out.println("Картку " + id + " заблоковано.");
                    }
                }
                case "3" -> {
                    String id = askCardId(in);
                    if (id != null) System.out.println(turnstile.pass(cards.get(id)));
                }
                case "4" -> {
                    String id = askCardId(in);
                    if (id != null) {
                        cards.get(id).damage();
                        System.out.println("Картка " + id + " пошкоджена (не зчитуватиметься).");
                    }
                }
                case "5" -> {
                    String s = ask(in, "Нова дата і час (yyyy-MM-dd HH:mm): ");
                    try {
                        clock.set(LocalDateTime.parse(s, FMT));
                    } catch (DateTimeParseException | NullPointerException e) {
                        System.out.println("Неправильний формат.");
                    }
                }
                case "6" -> listCards();
                case "7" -> System.out.print(turnstile.getStats().summaryReport());
                case "8" -> System.out.print(turnstile.getStats().byTypeReport());
                case "0" -> { return; }
                default -> System.out.println("Немає такого пункту.");
            }
        }
    }

    private void issueMenu(Scanner in) {
        SkiPassType[] types = SkiPassType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%2d - %s%n", i + 1, types[i].title());
        }
        String s = ask(in, "Тип: ");
        try {
            int n = Integer.parseInt(s.trim());
            Card card = system.issue(types[n - 1]);
            SkiPass pass = card.read();
            cards.put(pass.getId(), card);
            System.out.println("Видано: " + pass);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Неправильний номер типу.");
        } catch (Exception e) {
            System.out.println("Не вдалося випустити: " + e.getMessage());
        }
    }

    private String askCardId(Scanner in) {
        if (cards.isEmpty()) {
            System.out.println("Ще не видано жодної картки.");
            return null;
        }
        String id = ask(in, "Id картки (наприклад SP-000001): ");
        if (id == null || !cards.containsKey(id.trim())) {
            System.out.println("Картку не знайдено.");
            return null;
        }
        return id.trim();
    }

    private void listCards() {
        for (SkiPass p : system.allPasses()) {
            String blocked = system.isBlocked(p.getId()) ? "  [ЗАБЛОКОВАНО: " + system.blockReason(p.getId()) + "]" : "";
            System.out.println(p + blocked);
        }
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : null;
    }

    // сценарій

    private void demo() {
        say("Понеділок 14.12, 09:30. Видаємо картки");
        Card day = issueAndShow(SkiPassType.WORKDAY_1_DAY);
        Card lifts = issueAndShow(SkiPassType.WORKDAY_10_LIFTS);
        Card morning = issueAndShow(SkiPassType.WORKDAY_HALF_MORNING);
        Card afternoon = issueAndShow(SkiPassType.WORKDAY_HALF_AFTERNOON);
        Card weekend = issueAndShow(SkiPassType.WEEKEND_2_DAYS);
        Card season = issueAndShow(SkiPassType.SEASON);
        Card rule = issueAndShow(SkiPassType.WORKDAY_5_DAYS);

        say("Перші проходи");
        go("денний", day);
        go("10 підйомів", lifts);
        go("ранковий 9-13", morning);
        go("обідній 13-17 о 09:30", afternoon);
        go("вихідний у понеділок", weekend);
        go("сезонний", season);
        go("порушник (ще не заблокований)", rule);

        say("Порушника блокують у системі");
        system.block(idOf(rule), "проїзд без черги");
        go("порушник", rule);

        say("Пошкоджена і чужа картки");
        Card broken = issueAndShow(SkiPassType.WORKDAY_1_DAY);
        broken.damage();
        go("пошкоджена", broken);
        go("порожня картка", Card.blank());
        Card fake = new Card(new UnlimitedSkiPass("SP-999999", SkiPassType.SEASON,
                SEASON.start(), SEASON.end()));   // "підробка": дані є, але в реєстрі її немає
        go("підроблена сезонна", fake);

        say("Лижник з карткою на 10 підйомів катається, поки не скінчаться підйоми");
        for (int i = 0; i < 10; i++) {
            clock.plus(Duration.ofMinutes(15));
            go("10 підйомів", lifts);
        }

        say("13:30 - ранковий уже не діє, обідній вже діє");
        clock.set(LocalDateTime.of(2026, 12, 14, 13, 30));
        go("ранковий 9-13", morning);
        go("обідній 13-17", afternoon);

        say("17:10 - підйомник закритий");
        clock.set(LocalDateTime.of(2026, 12, 14, 17, 10));
        go("сезонний", season);

        say("Вівторок 15.12 - денний прострочений");
        clock.set(LocalDateTime.of(2026, 12, 15, 10, 0));
        go("денний (вчорашній)", day);

        say("Субота 19.12 - вихідний і сезонний діють, буденний - ні");
        clock.set(LocalDateTime.of(2026, 12, 19, 11, 0));
        Card workdayLifts = issueAndShow(SkiPassType.WORKDAY_20_LIFTS);
        go("вихідний 2 дні", weekend);
        go("сезонний", season);
        go("будні 20 підйомів у суботу", workdayLifts);

        say("ЗВІТ 1: сумарно");
        System.out.print(turnstile.getStats().summaryReport());
        say("ЗВІТ 2: по типах ski-pass");
        System.out.print(turnstile.getStats().byTypeReport());
    }

    private Card issueAndShow(SkiPassType type) {
        Card c = system.issue(type);
        cards.put(idOf(c), c);
        System.out.println("  видано: " + system.find(idOf(c)).orElseThrow());
        return c;
    }

    // id береться з самої картки
    private static String idOf(Card c) {
        try {
            return c.read().getId();
        } catch (Exception e) {
            throw new IllegalStateException("Картка не читається", e);
        }
    }

    private void go(String who, Card card) {
        System.out.printf("  %s  %-30s -> %s%n", clock.now().toLocalTime(), who, turnstile.pass(card));
    }

    private static void say(String s) {
        System.out.println();
        System.out.println("== " + s);
    }
}
