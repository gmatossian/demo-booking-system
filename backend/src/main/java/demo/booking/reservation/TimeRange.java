package demo.booking.reservation;

import java.time.Instant;

/** A half-open interval [start, end): a range ending at 11:00 does not overlap one starting at 11:00. */
public record TimeRange(Instant start, Instant end) {

    public TimeRange {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }
}
