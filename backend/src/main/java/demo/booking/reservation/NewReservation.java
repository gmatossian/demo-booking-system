package demo.booking.reservation;

import java.time.Instant;

/** Values for inserting a reservation row. */
public record NewReservation(
        long roomId,
        long personId,
        String title,
        TimeRange time,
        ReservationStatus status,
        Instant createdAt,
        Instant cancelledAt) {
}
