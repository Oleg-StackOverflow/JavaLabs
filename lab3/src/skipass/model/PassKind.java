package skipass.model;

// Як обмежується ski-pass.
public enum PassKind {
    // півдня, без обліку підйомів
    HALF_DAY,
    // N днів, без обліку підйомів
    DAYS,
    // N підйомів
    LIFTS,
    // абонемент на сезон
    SEASON
}
