package demo.booking.reservation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import demo.booking.person.PersonRepository;
import demo.booking.room.RoomRepository;
import demo.booking.venue.VenueProperties;
import demo.booking.web.ApiFieldError;
import demo.booking.web.ConflictException;
import demo.booking.web.InvalidRequestException;
import demo.booking.web.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Booking rules. The create and cancel operations are the authority; availability is only a preview. */
@Service
public class ReservationService {

    private final ReservationRepository reservations;
    private final RoomRepository rooms;
    private final PersonRepository people;
    private final BookingTimePolicy timePolicy;
    private final VenueProperties venue;
    private final Clock clock;

    ReservationService(ReservationRepository reservations, RoomRepository rooms, PersonRepository people,
            BookingTimePolicy timePolicy, VenueProperties venue, Clock clock) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.people = people;
        this.timePolicy = timePolicy;
        this.venue = venue;
        this.clock = clock;
    }

    public Reservation get(long id) {
        return reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation " + id + " does not exist."));
    }

    public List<Reservation> list(ReservationListView view) {
        return reservations.findForView(view, clock.instant());
    }

    /** Active reservations in the room during the given venue-local day. */
    public List<Reservation> schedule(long roomId, LocalDate date) {
        requireRoom(roomId);
        return reservations.findActiveOverlapping(roomId, timePolicy.day(date));
    }

    /**
     * Previews whether a range could be booked now. Applies the same time rules as
     * create but takes no lock, so the answer can be stale by the time of booking.
     */
    public List<Reservation> findConflicts(long roomId, LocalDateTime start, LocalDateTime end) {
        requireRoom(roomId);
        List<ApiFieldError> errors = timePolicy.check(start, end);
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
        return reservations.findActiveOverlapping(roomId, timePolicy.toRange(start, end));
    }

    @Transactional
    public Reservation create(CreateReservationRequest request) {
        List<ApiFieldError> errors = new ArrayList<>(timePolicy.check(request.start(), request.end()));
        if (rooms.findById(request.roomId()).isEmpty()) {
            errors.add(new ApiFieldError("roomId", "Choose one of the listed rooms."));
        }
        if (!people.existsById(request.personId())) {
            errors.add(new ApiFieldError("personId", "Choose one of the listed people."));
        }
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

        // Competing bookings for this room wait here until this transaction commits,
        // so the overlap check below always sees every previously accepted booking.
        if (!rooms.lockForBooking(request.roomId())) {
            throw new InvalidRequestException(List.of(new ApiFieldError("roomId", "Choose one of the listed rooms.")));
        }
        TimeRange time = timePolicy.toRange(request.start(), request.end());
        List<Reservation> conflicts = reservations.findActiveOverlapping(request.roomId(), time);
        if (!conflicts.isEmpty()) {
            throw new ConflictException("reservation_conflict",
                    "The room is already booked for part of that time.",
                    Map.of("conflicts", toResponses(conflicts)));
        }

        long id = reservations.insert(new NewReservation(request.roomId(), request.personId(), request.title(),
                time, ReservationStatus.ACTIVE, clock.instant(), null));
        return get(id);
    }

    @Transactional
    public Reservation cancel(long id) {
        if (!reservations.lockForUpdate(id)) {
            throw new NotFoundException("Reservation " + id + " does not exist.");
        }
        Reservation reservation = get(id);
        // Checked before the start-time rule so a repeated cancel is always a harmless no-op.
        if (reservation.status() == ReservationStatus.CANCELLED) {
            return reservation;
        }
        Instant now = clock.instant();
        if (!now.isBefore(reservation.time().start())) {
            throw new ConflictException("cancellation_not_allowed",
                    "This reservation has already started, so it can no longer be cancelled.", Map.of());
        }
        reservations.markCancelled(id, now);
        return get(id);
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.of(reservation, clock.instant(), venue.zone());
    }

    public List<ReservationResponse> toResponses(List<Reservation> list) {
        Instant now = clock.instant();
        return list.stream().map(r -> ReservationResponse.of(r, now, venue.zone())).toList();
    }

    private void requireRoom(long roomId) {
        if (rooms.findById(roomId).isEmpty()) {
            throw new NotFoundException("Room " + roomId + " does not exist.");
        }
    }
}
