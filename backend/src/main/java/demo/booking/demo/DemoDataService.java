package demo.booking.demo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import demo.booking.person.PersonRepository;
import demo.booking.reservation.NewReservation;
import demo.booking.reservation.ReservationRepository;
import demo.booking.reservation.ReservationStatus;
import demo.booking.reservation.TimeRange;
import demo.booking.room.RoomRepository;
import demo.booking.venue.VenueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Seeds and resets the demo dataset. The whole database is demo data. */
@Service
public class DemoDataService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);

    private final RoomRepository rooms;
    private final PersonRepository people;
    private final ReservationRepository reservations;
    private final VenueProperties venue;
    private final Clock clock;

    DemoDataService(RoomRepository rooms, PersonRepository people, ReservationRepository reservations,
            VenueProperties venue, Clock clock) {
        this.rooms = rooms;
        this.people = people;
        this.reservations = reservations;
        this.venue = venue;
        this.clock = clock;
    }

    /** Seeds only an empty database; existing data is always preserved. */
    @Transactional
    public boolean seedIfEmpty() {
        if (rooms.count() > 0) {
            return false;
        }
        insertFixtures();
        log.info("Seeded empty database with demo data");
        return true;
    }

    /**
     * Replaces all rooms, people, and reservations with fresh fixtures in one
     * transaction. Reservation IDs keep increasing and are never reused.
     */
    @Transactional
    public DemoDataSummary reset() {
        reservations.deleteAll();
        people.deleteAll();
        rooms.deleteAll();
        DemoDataSummary summary = insertFixtures();
        log.info("Reset demo data: {}", summary);
        return summary;
    }

    private DemoDataSummary insertFixtures() {
        DemoFixtures.ROOMS.forEach(rooms::insert);
        DemoFixtures.PEOPLE.forEach(people::insert);

        Instant now = clock.instant();
        LocalDate anchor = LocalDate.now(clock.withZone(venue.zone()));
        for (DemoFixtures.FixtureReservation fixture : DemoFixtures.RESERVATIONS) {
            LocalDate date = anchor.plusDays(fixture.dayOffset());
            TimeRange time = new TimeRange(
                    date.atTime(fixture.start()).atZone(venue.zone()).toInstant(),
                    date.atTime(fixture.end()).atZone(venue.zone()).toInstant());
            boolean cancelled = fixture.status() == ReservationStatus.CANCELLED;
            reservations.insert(new NewReservation(fixture.roomId(), fixture.personId(), fixture.title(),
                    time, fixture.status(), now, cancelled ? now : null));
        }
        return new DemoDataSummary(anchor, DemoFixtures.ROOMS.size(), DemoFixtures.PEOPLE.size(),
                DemoFixtures.RESERVATIONS.size());
    }

    public record DemoDataSummary(LocalDate anchorDate, int rooms, int people, int reservations) {
    }
}
