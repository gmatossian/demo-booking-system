package demo.booking.reservation;

import java.util.List;
import java.util.Locale;

import demo.booking.web.ApiFieldError;
import demo.booking.web.InvalidRequestException;

/** Filters for the reservation list. */
public enum ReservationListView {
    /** Active and not yet ended (includes in progress), soonest first. */
    UPCOMING,
    /** Active and ended, most recent first. */
    PAST,
    /** Cancelled, most recent start first. */
    CANCELLED,
    /** Everything, most recent start first. */
    ALL;

    static ReservationListView parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(List.of(new ApiFieldError(
                    "view", "Use one of: upcoming, past, cancelled, all.")));
        }
    }
}
