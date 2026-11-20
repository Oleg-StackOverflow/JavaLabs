package skipass.model;

/*
  Усі види ski-pass з умови. Кожен тип знає свою категорію днів,
  спосіб обмеження і "кількість" (днів або підйомів).
 */
public enum SkiPassType {
    // 1. Робочі дні, без обліку підйомів
    WORKDAY_HALF_MORNING(DayCategory.WORKDAY, PassKind.HALF_DAY, 1, HalfDay.MORNING, "Будні, пів дня 9-13"),
    WORKDAY_HALF_AFTERNOON(DayCategory.WORKDAY, PassKind.HALF_DAY, 1, HalfDay.AFTERNOON, "Будні, пів дня 13-17"),
    WORKDAY_1_DAY(DayCategory.WORKDAY, PassKind.DAYS, 1, null, "Будні, 1 день"),
    WORKDAY_2_DAYS(DayCategory.WORKDAY, PassKind.DAYS, 2, null, "Будні, 2 дні"),
    WORKDAY_5_DAYS(DayCategory.WORKDAY, PassKind.DAYS, 5, null, "Будні, 5 днів"),
    // 1. Робочі дні, по кількості підйомів
    WORKDAY_10_LIFTS(DayCategory.WORKDAY, PassKind.LIFTS, 10, null, "Будні, 10 підйомів"),
    WORKDAY_20_LIFTS(DayCategory.WORKDAY, PassKind.LIFTS, 20, null, "Будні, 20 підйомів"),
    WORKDAY_50_LIFTS(DayCategory.WORKDAY, PassKind.LIFTS, 50, null, "Будні, 50 підйомів"),
    WORKDAY_100_LIFTS(DayCategory.WORKDAY, PassKind.LIFTS, 100, null, "Будні, 100 підйомів"),
    // 2. Вихідні, без обліку підйомів
    WEEKEND_HALF_MORNING(DayCategory.WEEKEND, PassKind.HALF_DAY, 1, HalfDay.MORNING, "Вихідні, пів дня 9-13"),
    WEEKEND_HALF_AFTERNOON(DayCategory.WEEKEND, PassKind.HALF_DAY, 1, HalfDay.AFTERNOON, "Вихідні, пів дня 13-17"),
    WEEKEND_1_DAY(DayCategory.WEEKEND, PassKind.DAYS, 1, null, "Вихідні, 1 день"),
    WEEKEND_2_DAYS(DayCategory.WEEKEND, PassKind.DAYS, 2, null, "Вихідні, 2 дні"),
    // 2. Вихідні, по кількості підйомів
    WEEKEND_10_LIFTS(DayCategory.WEEKEND, PassKind.LIFTS, 10, null, "Вихідні, 10 підйомів"),
    WEEKEND_20_LIFTS(DayCategory.WEEKEND, PassKind.LIFTS, 20, null, "Вихідні, 20 підйомів"),
    WEEKEND_50_LIFTS(DayCategory.WEEKEND, PassKind.LIFTS, 50, null, "Вихідні, 50 підйомів"),
    WEEKEND_100_LIFTS(DayCategory.WEEKEND, PassKind.LIFTS, 100, null, "Вихідні, 100 підйомів"),
    // 3. Сезон
    SEASON(DayCategory.ANY, PassKind.SEASON, 0, null, "Абонемент на сезон");

    private final DayCategory days;
    private final PassKind kind;
    private final int amount;      // днів або підйомів, залежно від kind
    private final HalfDay halfDay; // тільки для HALF_DAY
    private final String title;

    SkiPassType(DayCategory days, PassKind kind, int amount, HalfDay halfDay, String title) {
        this.days = days;
        this.kind = kind;
        this.amount = amount;
        this.halfDay = halfDay;
        this.title = title;
    }

    public DayCategory days() { return days; }
    public PassKind kind() { return kind; }
    public int amount() { return amount; }
    public HalfDay halfDay() { return halfDay; }
    public String title() { return title; }

    public boolean countsLifts() {
        return kind == PassKind.LIFTS;
    }
}
