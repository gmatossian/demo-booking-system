package demo.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import demo.booking.venue.VenueProperties;
import demo.booking.web.ApiFieldError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingTimePolicyTest {

    private static final ZoneId LONDON = ZoneId.of("Europe/London");
    /** Tuesday 20 October 2026, 10:00 BST. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T09:00:00Z"), ZoneOffset.UTC);

    private final BookingTimePolicy policy = policy(LocalTime.of(8, 0), LocalTime.of(20, 0));

    @Test
    void acceptsRangeInsideOpeningHoursOnSlotBoundaries() {
        assertThat(policy.check(at("2026-10-21T08:00"), at("2026-10-21T20:00"))).isEmpty();
        assertThat(policy.check(at("2026-10-21T09:45"), at("2026-10-21T10:00"))).isEmpty();
    }

    @ParameterizedTest(name = "{0} → {1} is rejected on {2}")
    @CsvSource({
            "2026-10-21T10:00, 2026-10-21T10:00, end",   // empty range
            "2026-10-21T11:00, 2026-10-21T10:00, end",   // end before start
            "2026-10-21T10:10, 2026-10-21T11:00, start", // not on a 15-minute boundary
            "2026-10-21T07:45, 2026-10-21T09:00, start", // before opening
            "2026-10-21T19:00, 2026-10-21T20:15, end",   // after closing
            "2026-10-21T19:00, 2026-10-22T09:00, end",   // spans two days
            "2026-10-20T09:45, 2026-10-20T10:30, start", // already started (now is 10:00)
            "2027-01-19T09:00, 2027-01-19T10:00, start", // beyond the 90-day horizon
    })
    void rejectsInvalidRanges(String start, String end, String field) {
        assertThat(policy.check(at(start), at(end))).extracting(ApiFieldError::field).contains(field);
    }

    @Test
    void acceptsStartExactlyNowAndLastBookableDay() {
        assertThat(policy.check(at("2026-10-20T10:00"), at("2026-10-20T10:15"))).isEmpty();
        assertThat(policy.check(at("2027-01-18T19:45"), at("2027-01-18T20:00"))).isEmpty();
    }

    @Test
    void convertsUsingTheOffsetInForceOnEachSideOfTheAutumnClockChange() {
        // Clocks go back at 02:00 BST on Sunday 25 October 2026.
        assertThat(policy.toRange(at("2026-10-24T09:00"), at("2026-10-24T10:00")).start())
                .isEqualTo(Instant.parse("2026-10-24T08:00:00Z"));
        assertThat(policy.toRange(at("2026-10-25T09:00"), at("2026-10-25T10:00")).start())
                .isEqualTo(Instant.parse("2026-10-25T09:00:00Z"));
        // The clock-change day is 25 hours long.
        TimeRange day = policy.day(at("2026-10-25T00:00").toLocalDate());
        assertThat(java.time.Duration.between(day.start(), day.end())).hasHours(25);
    }

    @Test
    void rejectsNonexistentAndAmbiguousLocalTimesWhenHoursWouldAllowThem() {
        // With the real 08:00–20:00 hours these times are already outside opening hours;
        // a 24-hour venue shows the daylight-saving rule itself.
        BookingTimePolicy allDay = policy(LocalTime.MIN, LocalTime.of(23, 45));
        // 01:30 is repeated on 25 October 2026 (autumn) ...
        assertThat(allDay.check(at("2026-10-25T01:30"), at("2026-10-25T03:00")))
                .extracting(ApiFieldError::field).containsExactly("start");
        // ... and skipped on 28 March 2027 (spring), though that is beyond the horizon too.
        assertThat(allDay.check(at("2027-03-28T01:00"), at("2027-03-28T01:30")))
                .extracting(ApiFieldError::message)
                .anyMatch(message -> message.contains("daylight-saving"));
    }

    private static BookingTimePolicy policy(LocalTime opensAt, LocalTime closesAt) {
        return new BookingTimePolicy(
                new VenueProperties("Test venue", LONDON, opensAt, closesAt, 15, 90), CLOCK);
    }

    private static LocalDateTime at(String value) {
        return LocalDateTime.parse(value);
    }
}
