package skipass;

import org.junit.Test;

import skipass.model.*;

import java.time.LocalDate;

import static org.junit.Assert.*;

// Перевірки самої моделі: категорії днів, межі пів дня, захист від неправильних даних.
public class SkiPassModelTest {

    private static final LocalDate D = LocalDate.of(2026, 12, 14);

    @Test
    public void dayCategories() {
        LocalDate fri = LocalDate.of(2026, 12, 18), sat = fri.plusDays(1), sun = fri.plusDays(2);
        assertTrue(DayCategory.WORKDAY.matches(fri));
        assertFalse(DayCategory.WORKDAY.matches(sat));
        assertTrue(DayCategory.WEEKEND.matches(sun));
        assertFalse(DayCategory.WEEKEND.matches(fri));
        assertTrue(DayCategory.ANY.matches(sat));
    }

    @Test
    public void allTypesFromTaskArePresent() {
        assertEquals(18, SkiPassType.values().length); // 5+4 будні, 3+4 вихідні, 1 сезон
        long lifts = java.util.Arrays.stream(SkiPassType.values()).filter(SkiPassType::countsLifts).count();
        assertEquals(8, lifts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void liftPassWithWrongTypeRejected() {
        new LiftCountSkiPass("X", SkiPassType.SEASON, D, D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void halfDayWithWrongTypeRejected() {
        new HalfDaySkiPass("X", SkiPassType.WORKDAY_1_DAY, D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validUntilBeforeFromRejected() {
        new UnlimitedSkiPass("X", SkiPassType.WORKDAY_1_DAY, D, D.minusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooManyLiftsRejected() {
        new LiftCountSkiPass("X", SkiPassType.WORKDAY_10_LIFTS, D, D, 11);
    }

    @Test(expected = IllegalStateException.class)
    public void registerRideOnEmptyLiftPassThrows() {
        new LiftCountSkiPass("X", SkiPassType.WORKDAY_10_LIFTS, D, D, 0).registerRide();
    }
}
