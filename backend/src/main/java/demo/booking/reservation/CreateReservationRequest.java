package demo.booking.reservation;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Start and end are venue-local wall-clock times, e.g. {@code "2026-10-25T09:00"}. */
public record CreateReservationRequest(
        @NotNull(message = "Choose a room.") Long roomId,
        @NotNull(message = "Choose who the reservation is for.") Long personId,
        @NotBlank(message = "Enter a title.")
        @Size(max = 80, message = "Title must be 80 characters or fewer.") String title,
        @NotNull(message = "Choose a start time.") LocalDateTime start,
        @NotNull(message = "Choose an end time.") LocalDateTime end) {

    public CreateReservationRequest {
        title = title == null ? null : title.strip();
    }
}
