package skipass;

import org.junit.Before;
import org.junit.Test;

import skipass.app.SimulatedClock;
import skipass.model.*;
import skipass.system.Season;
import skipass.system.SkiPassSystem;
import skipass.turnstile.AccessResult;
import skipass.turnstile.Turnstile;
import skipass.turnstile.TurnstileStats;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

// Перевірки турнікету: кожна причина відмови + облік.
public class TurnstileTest {

    private static final LocalDate MON = LocalDate.of(2026, 12, 14);
    private static final LocalDate SAT = LocalDate.of(2026, 12, 19);
    private static final Season SEASON = new Season(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 3, 31));

    private SimulatedClock clock;
    private SkiPassSystem system;
    private Turnstile turnstile;

    @Before
    public void setUp() {
        clock = new SimulatedClock(MON.atTime(10, 0));
        system = new SkiPassSystem(clock, SEASON);
        turnstile = new Turnstile("T1", system, clock);
    }

    private void at(LocalDate d, int h, int m) {
        clock.set(d.atTime(h, m));
    }

    private static String id(Card c) throws CardReadException {
        return c.read().getId();
    }

    private void assertDenied(DenyReason expected, AccessResult r) {
        assertFalse("очікувалась відмова " + expected, r.allowed());
        assertEquals(expected, r.reason());
    }

    // ---------- дозволи ----------

    @Test
    public void validDayPassIsAllowedManyTimes() {
        Card c = system.issue(SkiPassType.WORKDAY_1_DAY);
        for (int i = 0; i < 30; i++) {
            assertTrue(turnstile.pass(c).allowed()); // без обліку підйомів
        }
    }

    @Test
    public void liftPassLosesOneLiftPerPass() throws Exception {
        Card c = system.issue(SkiPassType.WORKDAY_10_LIFTS);
        turnstile.pass(c);
        turnstile.pass(c);
        assertEquals(8, ((LiftCountSkiPass) c.read()).getLiftsLeft());
    }

    // ---------- кожна причина відмови ----------

    @Test
    public void damagedCardIsUnreadable() {
        Card c = system.issue(SkiPassType.SEASON);
        c.damage();
        AccessResult r = turnstile.pass(c);
        assertDenied(DenyReason.UNREADABLE, r);
        assertNull(r.type());
    }

    @Test
    public void blankCardIsUnreadable() {
        assertDenied(DenyReason.UNREADABLE, turnstile.pass(Card.blank()));
    }

    @Test
    public void forgedCardIsUnknown() {
        Card fake = new Card(new UnlimitedSkiPass("SP-999999", SkiPassType.SEASON, SEASON.start(), SEASON.end()));
        assertDenied(DenyReason.UNKNOWN_CARD, turnstile.pass(fake));
    }

    @Test
    public void blockedCardIsDenied() throws Exception {
        Card c = system.issue(SkiPassType.SEASON);
        assertTrue(turnstile.pass(c).allowed());
        system.block(id(c), "порушення");
        assertDenied(DenyReason.BLOCKED, turnstile.pass(c));
    }

    @Test
    public void blockedLiftCardKeepsItsLifts() throws Exception {
        Card c = system.issue(SkiPassType.WORKDAY_10_LIFTS);
        system.block(id(c), "порушення");
        turnstile.pass(c);
        assertEquals(10, ((LiftCountSkiPass) c.read()).getLiftsLeft());
    }

    @Test
    public void weekendPassOnMondayIsNotYetValid() {
        Card c = system.issue(SkiPassType.WEEKEND_1_DAY); // діє з суботи
        assertDenied(DenyReason.NOT_YET_VALID, turnstile.pass(c));
        at(SAT, 10, 0);
        assertTrue(turnstile.pass(c).allowed());
    }

    @Test
    public void dayPassExpiresNextDay() {
        Card c = system.issue(SkiPassType.WORKDAY_1_DAY);
        at(MON.plusDays(1), 10, 0);
        assertDenied(DenyReason.EXPIRED, turnstile.pass(c));
    }

    @Test
    public void workdayPassOnSaturdayIsWrongDay() {
        Card c = system.issue(SkiPassType.WORKDAY_5_DAYS); // пн-пт, але й субота між ними не входить
        at(SAT, 10, 0);
        assertDenied(DenyReason.EXPIRED, turnstile.pass(c)); // субота вже після п'ятниці
        Card lifts = system.issue(SkiPassType.WORKDAY_20_LIFTS);
        assertDenied(DenyReason.WRONG_DAY, turnstile.pass(lifts));
    }

    @Test
    public void workdayFiveDaysFromThursdayIsWrongDayOnSaturdayButValidOnMonday() {
        at(MON.plusDays(3), 10, 0); // четвер
        Card c = system.issue(SkiPassType.WORKDAY_5_DAYS);
        at(SAT, 10, 0);
        assertDenied(DenyReason.WRONG_DAY, turnstile.pass(c));
        at(SAT.plusDays(2), 10, 0); // понеділок
        assertTrue(turnstile.pass(c).allowed());
    }

    @Test
    public void weekendLiftsOnWorkdayIsWrongDay() {
        Card c = system.issue(SkiPassType.WEEKEND_10_LIFTS);
        assertDenied(DenyReason.WRONG_DAY, turnstile.pass(c));
    }

    @Test
    public void liftIsClosedBeforeNineAndFromFive() {
        Card c = system.issue(SkiPassType.SEASON);
        at(MON, 8, 59);
        assertDenied(DenyReason.LIFT_CLOSED, turnstile.pass(c));
        at(MON, 9, 0);
        assertTrue(turnstile.pass(c).allowed());
        at(MON, 16, 59);
        assertTrue(turnstile.pass(c).allowed());
        at(MON, 17, 0);
        assertDenied(DenyReason.LIFT_CLOSED, turnstile.pass(c));
    }

    @Test
    public void halfDayBoundaryAtOnePm() {
        Card morning = system.issue(SkiPassType.WORKDAY_HALF_MORNING);
        Card afternoon = system.issue(SkiPassType.WORKDAY_HALF_AFTERNOON);
        at(MON, 12, 59);
        assertTrue(turnstile.pass(morning).allowed());
        assertDenied(DenyReason.OUTSIDE_HALF_DAY, turnstile.pass(afternoon));
        at(MON, 13, 0);
        assertDenied(DenyReason.OUTSIDE_HALF_DAY, turnstile.pass(morning));
        assertTrue(turnstile.pass(afternoon).allowed());
    }

    @Test
    public void liftPassRunsOut() {
        Card c = system.issue(SkiPassType.WORKDAY_10_LIFTS);
        for (int i = 0; i < 10; i++) {
            assertTrue(turnstile.pass(c).allowed());
        }
        assertDenied(DenyReason.NO_LIFTS_LEFT, turnstile.pass(c));
    }

    @Test
    public void deniedPassDoesNotSpendLift() throws Exception {
        Card c = system.issue(SkiPassType.WORKDAY_10_LIFTS);
        at(MON, 18, 0);
        turnstile.pass(c);
        assertEquals(10, ((LiftCountSkiPass) c.read()).getLiftsLeft());
    }

    // ---------- облік ----------

    @Test
    public void statsCountTotalsTypesAndReasons() {
        Card season = system.issue(SkiPassType.SEASON);
        Card lifts = system.issue(SkiPassType.WORKDAY_10_LIFTS);
        Card weekend = system.issue(SkiPassType.WEEKEND_2_DAYS);

        turnstile.pass(season);
        turnstile.pass(season);
        turnstile.pass(lifts);
        turnstile.pass(weekend);          // NOT_YET_VALID
        turnstile.pass(Card.blank());     // UNREADABLE

        TurnstileStats s = turnstile.getStats();
        assertEquals(3, s.allowed());
        assertEquals(2, s.denied());
        assertEquals(2, s.forType(SkiPassType.SEASON).allowed());
        assertEquals(1, s.forType(SkiPassType.WORKDAY_10_LIFTS).allowed());
        assertEquals(1, s.forType(SkiPassType.WEEKEND_2_DAYS).denied());
        assertEquals(0, s.forType(SkiPassType.WEEKEND_100_LIFTS).total());
        assertEquals(1, s.deniedFor(DenyReason.UNREADABLE));
        assertEquals(1, s.deniedFor(DenyReason.NOT_YET_VALID));

        String byType = s.byTypeReport();
        assertTrue(byType.contains("Абонемент на сезон"));
        assertTrue(byType.contains("(не зчитано)"));
        assertTrue(s.summaryReport().contains("Всього спроб: 5"));
    }
}
