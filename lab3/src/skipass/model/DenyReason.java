package skipass.model;

// Причини відмови в проході.
public enum DenyReason {
    UNREADABLE("не вдалося зчитати картку"),
    UNKNOWN_CARD("картки немає в реєстрі"),
    BLOCKED("картку заблоковано"),
    NOT_YET_VALID("термін дії ще не почався"),
    EXPIRED("термін дії минув"),
    WRONG_DAY("картка не діє в цей день тижня"),
    LIFT_CLOSED("підйомник зараз не працює (9:00-17:00)"),
    OUTSIDE_HALF_DAY("картка на півдня діє в інший час"),
    NO_LIFTS_LEFT("не залишилось підйомів");

    private final String message;

    DenyReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
