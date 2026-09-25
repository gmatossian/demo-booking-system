package demo.booking.venue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes venue rules so the frontend never has to guess the timezone or hours. */
@RestController
class VenueController {

    private final VenueProperties venue;
    private final Clock clock;

    VenueController(VenueProperties venue, Clock clock) {
        this.venue = venue;
        this.clock = clock;
    }

    @GetMapping("/api/venue")
    VenueResponse venue() {
        OffsetDateTime now = OffsetDateTime.now(clock.withZone(venue.zone()));
        LocalDate today = now.toLocalDate();
        return new VenueResponse(
                venue.name(),
                venue.zone().getId(),
                venue.opensAt(),
                venue.closesAt(),
                venue.slotMinutes(),
                venue.bookingHorizonDays(),
                now,
                today,
                today.plusDays(venue.bookingHorizonDays()));
    }

    record VenueResponse(
            String name,
            String zone,
            @JsonFormat(pattern = "HH:mm") LocalTime opensAt,
            @JsonFormat(pattern = "HH:mm") LocalTime closesAt,
            int slotMinutes,
            int bookingHorizonDays,
            OffsetDateTime now,
            LocalDate today,
            LocalDate lastBookableDate) {
    }
}
