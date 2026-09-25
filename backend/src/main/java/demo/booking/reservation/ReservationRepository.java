package demo.booking.reservation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepository {

    private static final String SELECT_RESERVATION = """
            SELECT r.id, r.room_id, rm.name AS room_name, r.person_id, p.display_name AS person_name,
                   r.title, r.start_at, r.end_at, r.status, r.created_at, r.cancelled_at
            FROM reservation r
            JOIN room rm ON rm.id = r.room_id
            JOIN person p ON p.id = r.person_id
            """;

    private final JdbcClient jdbc;

    ReservationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Reservation> findById(long id) {
        return jdbc.sql(SELECT_RESERVATION + "WHERE r.id = :id")
                .param("id", id)
                .query(ReservationRepository::mapReservation)
                .optional();
    }

    /** Locks the reservation row until the current transaction ends; false if it does not exist. */
    public boolean lockForUpdate(long id) {
        return jdbc.sql("SELECT id FROM reservation WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(Long.class)
                .optional()
                .isPresent();
    }

    /** Active reservations for the room that overlap the half-open range [start, end). */
    public List<Reservation> findActiveOverlapping(long roomId, TimeRange range) {
        return jdbc.sql(SELECT_RESERVATION + """
                        WHERE r.room_id = :roomId
                          AND r.status = 'ACTIVE'
                          AND r.start_at < :end
                          AND r.end_at > :start
                        ORDER BY r.start_at
                        """)
                .param("roomId", roomId)
                .param("start", utc(range.start()))
                .param("end", utc(range.end()))
                .query(ReservationRepository::mapReservation)
                .list();
    }

    public List<Reservation> findForView(ReservationListView view, Instant now) {
        String where = switch (view) {
            case UPCOMING -> "WHERE r.status = 'ACTIVE' AND r.end_at > :now ORDER BY r.start_at, r.id";
            case PAST -> "WHERE r.status = 'ACTIVE' AND r.end_at <= :now ORDER BY r.start_at DESC, r.id DESC";
            case CANCELLED -> "WHERE r.status = 'CANCELLED' ORDER BY r.start_at DESC, r.id DESC";
            case ALL -> "ORDER BY r.start_at DESC, r.id DESC";
        };
        // Named parameters that a query does not use are ignored.
        return jdbc.sql(SELECT_RESERVATION + where)
                .param("now", utc(now))
                .query(ReservationRepository::mapReservation)
                .list();
    }

    public long insert(NewReservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("""
                        INSERT INTO reservation
                            (room_id, person_id, title, start_at, end_at, status, created_at, cancelled_at)
                        VALUES
                            (:roomId, :personId, :title, :start, :end, :status, :createdAt, :cancelledAt)
                        """)
                .param("roomId", reservation.roomId())
                .param("personId", reservation.personId())
                .param("title", reservation.title())
                .param("start", utc(reservation.time().start()))
                .param("end", utc(reservation.time().end()))
                .param("status", reservation.status().name())
                .param("createdAt", utc(reservation.createdAt()))
                .param("cancelledAt", utc(reservation.cancelledAt()))
                .update(keyHolder, "id");
        return keyHolder.getKeyAs(Long.class);
    }

    public void markCancelled(long id, Instant cancelledAt) {
        jdbc.sql("UPDATE reservation SET status = 'CANCELLED', cancelled_at = :cancelledAt WHERE id = :id")
                .param("id", id)
                .param("cancelledAt", utc(cancelledAt))
                .update();
    }

    public void deleteAll() {
        jdbc.sql("DELETE FROM reservation").update();
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static Reservation mapReservation(ResultSet rs, int rowNum) throws SQLException {
        return new Reservation(
                rs.getLong("id"),
                rs.getLong("room_id"),
                rs.getString("room_name"),
                rs.getLong("person_id"),
                rs.getString("person_name"),
                rs.getString("title"),
                new TimeRange(instant(rs, "start_at"), instant(rs, "end_at")),
                ReservationStatus.valueOf(rs.getString("status")),
                instant(rs, "created_at"),
                instant(rs, "cancelled_at"));
    }
}
