package demo.booking.reservation;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * API view of a reservation. Times carry the venue's UTC offset for that moment.
 * {@code phase} is derived from the current time and is independent of status.
 */
public record ReservationResponse(
        long id,
        long roomId,
        String roomName,
        long personId,
        String personName,
        String title,
        OffsetDateTime start,
        OffsetDateTime end,
        ReservationStatus status,
        Phase phase,
        boolean cancellable,
        OffsetDateTime createdAt,
        OffsetDateTime cancelledAt) {

    public enum Phase {
        UPCOMING,
        IN_PROGRESS,
        ENDED
    }

    public static ReservationResponse of(Reservation r, Instant now, ZoneId zone) {
        Phase phase = now.isBefore(r.time().start()) ? Phase.UPCOMING
                : now.isBefore(r.time().end()) ? Phase.IN_PROGRESS
                : Phase.ENDED;
        boolean cancellable = r.status() == ReservationStatus.ACTIVE && phase == Phase.UPCOMING;
        return new ReservationResponse(
                r.id(),
                r.roomId(),
                r.roomName(),
                r.personId(),
                r.personName(),
                r.title(),
                atVenue(r.time().start(), zone),
                atVenue(r.time().end(), zone),
                r.status(),
                phase,
                cancellable,
                atVenue(r.createdAt(), zone),
                atVenue(r.cancelledAt(), zone));
    }

    private static OffsetDateTime atVenue(Instant instant, ZoneId zone) {
        return instant == null ? null : instant.atZone(zone).toOffsetDateTime();
    }
}
