package demo.booking.demo;

import java.time.LocalTime;
import java.util.List;

import demo.booking.person.Person;
import demo.booking.reservation.ReservationStatus;
import demo.booking.room.Room;

/**
 * The fictional demo dataset. Reservation dates are day offsets from the venue's
 * current date at seed/reset time, so the data stays useful as time passes.
 */
final class DemoFixtures {

    static final List<Room> ROOMS = List.of(
            new Room(1, "Aurora", "Floor 1, east wing", 4, List.of("Display screen", "Whiteboard")),
            new Room(2, "Beacon", "Floor 1, west wing", 8, List.of("Display screen", "Video conferencing")),
            new Room(3, "Cedar", "Floor 2", 12, List.of("Projector", "Whiteboard", "Video conferencing")),
            new Room(4, "Driftwood", "Floor 2, quiet zone", 2, List.of("Display screen")),
            new Room(5, "Evergreen Hall", "Floor 3", 30, List.of("Projector", "Microphones", "Video conferencing")));

    static final List<Person> PEOPLE = List.of(
            new Person(1, "Avery Quinn"),
            new Person(2, "Jordan Blake"),
            new Person(3, "Morgan Ellis"),
            new Person(4, "Riley Hart"),
            new Person(5, "Sam Rowe"));

    record FixtureReservation(
            long roomId, long personId, String title,
            int dayOffset, LocalTime start, LocalTime end, ReservationStatus status) {
    }

    static final List<FixtureReservation> RESERVATIONS = List.of(
            active(1, 1, "Weekly planning", -2, "09:00", "10:00"),
            active(3, 3, "Design review", -1, "14:00", "15:30"),
            cancelled(2, 2, "Supplier call", -1, "11:00", "12:00"),
            active(4, 4, "Lunch-and-learn prep", 0, "12:00", "13:00"),
            // Back-to-back pair: adjacent reservations do not overlap.
            active(1, 1, "Team stand-up", 1, "09:00", "10:00"),
            active(1, 2, "1:1 catch-up", 1, "10:00", "11:00"),
            active(3, 3, "Quarterly roadmap workshop", 1, "13:00", "15:00"),
            // Cancelled, so this slot is free again.
            cancelled(2, 4, "Customer demo rehearsal", 2, "10:30", "11:30"),
            active(2, 5, "Hiring panel", 2, "15:00", "16:00"),
            active(5, 1, "All-hands", 3, "09:30", "12:00"),
            active(4, 4, "Focus call", 7, "16:00", "16:30"));

    private DemoFixtures() {
    }

    private static FixtureReservation active(long room, long person, String title, int day, String start, String end) {
        return new FixtureReservation(room, person, title, day, LocalTime.parse(start), LocalTime.parse(end),
                ReservationStatus.ACTIVE);
    }

    private static FixtureReservation cancelled(long room, long person, String title, int day, String start, String end) {
        return new FixtureReservation(room, person, title, day, LocalTime.parse(start), LocalTime.parse(end),
                ReservationStatus.CANCELLED);
    }
}
