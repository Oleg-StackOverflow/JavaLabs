package skipass;

import org.junit.Before;
import org.junit.Test;

import skipass.app.SimulatedClock;
import skipass.model.*;
import skipass.system.Season;
import skipass.system.SkiPassSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

// Випуск карток: правильні терміни дії для кожного виду.
public class SkiPassSystemTest {

    // 14.12.2026 - понеділок
    private static final LocalDate MON = LocalDate.of(2026, 12, 14);
    private static final Season SEASON = new Season(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 3, 31));

    private SimulatedClock clock;
    private SkiPassSystem system;

    @Before
    public void setUp() {
        clock = new SimulatedClock(MON.atTime(10, 0));
        system = new SkiPassSystem(clock, SEASON);
    }

    private SkiPass issue(SkiPassType t) throws CardReadException {
        return system.issue(t).read();
    }

    @Test
    public void idsAreUniqueAndSequential() throws Exception {
        assertEquals("SP-000001", issue(SkiPassType.WORKDAY_1_DAY).getId());
        assertEquals("SP-000002", issue(SkiPassType.SEASON).getId());
        assertEquals(2, system.allPasses().size());
    }

    @Test
    public void everyTypeCanBeIssued() {
        for (SkiPassType t : SkiPassType.values()) {
            assertNotNull(t.name(), system.issue(t));
        }
        assertEquals(SkiPassType.values().length, system.allPasses().size());
    }

    @Test
    public void workdayOneDayIsValidToday() throws Exception {
        SkiPass p = issue(SkiPassType.WORKDAY_1_DAY);
        assertEquals(MON, p.getValidFrom());
        assertEquals(MON, p.getValidUntil());
        assertTrue(p instanceof UnlimitedSkiPass);
    }

    @Test
    public void workdayFiveDaysIsMondayToFriday() throws Exception {
        SkiPass p = issue(SkiPassType.WORKDAY_5_DAYS);
        assertEquals(MON, p.getValidFrom());
        assertEquals(MON.plusDays(4), p.getValidUntil());
    }

    @Test
    public void workdayFiveDaysFromThursdaySkipsWeekend() throws Exception {
        clock.set(MON.plusDays(3).atTime(10, 0)); // четвер 17.12
        SkiPass p = issue(SkiPassType.WORKDAY_5_DAYS);
        // чт, пт, пн, вт, ср
        assertEquals(LocalDate.of(2026, 12, 23), p.getValidUntil());
    }

    @Test
    public void weekendPassBoughtOnMondayStartsOnSaturday() throws Exception {
        SkiPass p = issue(SkiPassType.WEEKEND_2_DAYS);
        assertEquals(LocalDate.of(2026, 12, 19), p.getValidFrom());
        assertEquals(LocalDate.of(2026, 12, 20), p.getValidUntil());
    }

    @Test
    public void workdayHalfDayBoughtOnSaturdayIsForMonday() throws Exception {
        clock.set(LocalDateTime.of(2026, 12, 19, 10, 0));
        SkiPass p = issue(SkiPassType.WORKDAY_HALF_MORNING);
        assertEquals(LocalDate.of(2026, 12, 21), p.getValidFrom());
        assertTrue(p instanceof HalfDaySkiPass);
    }

    @Test
    public void liftPassIsValidUntilSeasonEnd() throws Exception {
        SkiPass p = issue(SkiPassType.WEEKEND_50_LIFTS);
        assertEquals(SEASON.end(), p.getValidUntil());
        assertEquals(50, ((LiftCountSkiPass) p).getLiftsLeft());
    }

    @Test
    public void seasonPassCoversWholeSeason() throws Exception {
        SkiPass p = issue(SkiPassType.SEASON);
        assertEquals(SEASON.start(), p.getValidFrom());
        assertEquals(SEASON.end(), p.getValidUntil());
    }

    @Test
    public void passBoughtBeforeSeasonStartsWithSeason() throws Exception {
        clock.set(LocalDateTime.of(2026, 11, 20, 10, 0));
        assertEquals(SEASON.start(), issue(SkiPassType.WORKDAY_10_LIFTS).getValidFrom());
    }

    @Test
    public void multiDayPassIsCutAtSeasonEnd() throws Exception {
        clock.set(LocalDateTime.of(2027, 3, 31, 10, 0)); // середа, останній день сезону
        assertEquals(SEASON.end(), issue(SkiPassType.WORKDAY_5_DAYS).getValidUntil());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotIssueAfterSeason() {
        clock.set(LocalDateTime.of(2027, 4, 2, 10, 0));
        system.issue(SkiPassType.SEASON);
    }

    @Test
    public void blockMarksPass() throws Exception {
        String id = issue(SkiPassType.SEASON).getId();
        assertFalse(system.isBlocked(id));
        system.block(id, "проїзд без черги");
        assertTrue(system.isBlocked(id));
        assertEquals("проїзд без черги", system.blockReason(id));
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void blockUnknownThrows() {
        system.block("SP-123456", "x");
    }
}
