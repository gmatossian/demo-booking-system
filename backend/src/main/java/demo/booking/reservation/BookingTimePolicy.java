package demo.booking.reservation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import demo.booking.venue.VenueProperties;
import demo.booking.web.ApiFieldError;
import org.springframework.stereotype.Component;

/**
 * Time rules for new bookings, evaluated in the venue timezone (never the
 * server's or browser's). Requested times are venue-local wall-clock times.
 */
@Component
public class BookingTimePolicy {

    private final VenueProperties venue;
    private final Clock clock;

    public BookingTimePolicy(VenueProperties venue, Clock clock) {
        this.venue = venue;
        this.clock = clock;
    }

    /** Returns every rule the requested range breaks; empty means it may be booked if the room is free. */
    public List<ApiFieldError> check(LocalDateTime start, LocalDateTime end) {
        List<ApiFieldError> errors = new ArrayList<>();
        checkOnSlotBoundary("start", start, errors);
        checkOnSlotBoundary("end", end, errors);

        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (!sameDay) {
            errors.add(new ApiFieldError("end", "A reservation must start and end on the same day."));
        } else if (!end.isAfter(start)) {
            errors.add(new ApiFieldError("end", "End time must be after the start time."));
        }

        LocalTime startTime = start.toLocalTime();
        if (startTime.isBefore(venue.opensAt()) || !startTime.isBefore(venue.closesAt())) {
            errors.add(new ApiFieldError("start", "Start time must be within opening hours " + openingHours() + "."));
        }
        LocalTime endTime = end.toLocalTime();
        if (sameDay && (endTime.isAfter(venue.closesAt()) || !endTime.isAfter(venue.opensAt()))) {
            errors.add(new ApiFieldError("end", "End time must be within opening hours " + openingHours() + "."));
        }

        boolean startUnambiguous = checkUnambiguous("start", start, errors);
        checkUnambiguous("end", end, errors);

        if (startUnambiguous && toInstant(start).isBefore(clock.instant())) {
            errors.add(new ApiFieldError("start", "Start time has already passed."));
        }
        LocalDate lastBookableDate = today().plusDays(venue.bookingHorizonDays());
        if (start.toLocalDate().isAfter(lastBookableDate)) {
            errors.add(new ApiFieldError("start", "Reservations can be made up to "
                    + venue.bookingHorizonDays() + " days ahead (until " + lastBookableDate + ")."));
        }
        return errors;
    }

    /** Converts an already-checked venue-local range to instants. */
    public TimeRange toRange(LocalDateTime start, LocalDateTime end) {
        return new TimeRange(toInstant(start), toInstant(end));
    }

    /** The whole venue-local calendar day as instants (not always 24 hours long). */
    public TimeRange day(LocalDate date) {
        return new TimeRange(
                date.atStartOfDay(venue.zone()).toInstant(),
                date.plusDays(1).atStartOfDay(venue.zone()).toInstant());
    }

    public LocalDate today() {
        return LocalDate.now(clock.withZone(venue.zone()));
    }

    private Instant toInstant(LocalDateTime venueLocal) {
        return venueLocal.atZone(venue.zone()).toInstant();
    }

    private void checkOnSlotBoundary(String field, LocalDateTime time, List<ApiFieldError> errors) {
        if (time.getMinute() % venue.slotMinutes() != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            errors.add(new ApiFieldError(field, "Times must be on a " + venue.slotMinutes() + "-minute boundary."));
        }
    }

    /** Rejects wall-clock times skipped or repeated by a daylight-saving change. */
    private boolean checkUnambiguous(String field, LocalDateTime time, List<ApiFieldError> errors) {
        if (venue.zone().getRules().getValidOffsets(time).size() == 1) {
            return true;
        }
        errors.add(new ApiFieldError(field, "This time does not exist or is ambiguous in "
                + venue.zone().getId() + " because of a daylight-saving change."));
        return false;
    }

    private String openingHours() {
        return venue.opensAt() + "–" + venue.closesAt();
    }
}
