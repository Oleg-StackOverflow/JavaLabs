package skipass.model;

/*
 Фізична картка. На ній записані дані SkiPass, але картку можна пошкодити -
 тоді дані не зчитуються.
 */
public class Card {

    private final SkiPass data;
    private boolean damaged;

    public Card(SkiPass data) {
        this.data = data;
    }

    // Картка, з якої нічого не можна зчитати
    public static Card blank() {
        return new Card(null);
    }

    public SkiPass read() throws CardReadException {
        if (damaged || data == null) {
            throw new CardReadException("Помилка зчитування картки");
        }
        return data;
    }

    public void damage() {
        damaged = true;
    }

    public boolean isDamaged() {
        return damaged;
    }

    @Override
    public String toString() {
        if (data == null) return "Card[порожня]";
        return "Card[" + data.getId() + (damaged ? ", пошкоджена" : "") + "]";
    }
}
