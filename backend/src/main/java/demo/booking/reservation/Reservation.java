package demo.booking.reservation;

import java.time.Instant;

/** A stored reservation, joined with its room and booker names. */
public record Reservation(
        long id,
        long roomId,
        String roomName,
        long personId,
        String personName,
        String title,
        TimeRange time,
        ReservationStatus status,
        Instant createdAt,
        Instant cancelledAt) {
}
