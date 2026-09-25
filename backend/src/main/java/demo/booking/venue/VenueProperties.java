package demo.booking.venue;

import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** The single fictional venue whose timezone and opening hours govern every booking. */
@ConfigurationProperties("venue")
public record VenueProperties(
        String name,
        ZoneId zone,
        LocalTime opensAt,
        LocalTime closesAt,
        int slotMinutes,
        int bookingHorizonDays) {
}
