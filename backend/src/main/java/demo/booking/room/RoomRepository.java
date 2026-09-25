package demo.booking.room;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

    private final JdbcClient jdbc;

    RoomRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Room> findAll() {
        return jdbc.sql("SELECT id, name, location, capacity, equipment FROM room ORDER BY name")
                .query(RoomRepository::mapRoom)
                .list();
    }

    public Optional<Room> findById(long id) {
        return jdbc.sql("SELECT id, name, location, capacity, equipment FROM room WHERE id = :id")
                .param("id", id)
                .query(RoomRepository::mapRoom)
                .optional();
    }

    /**
     * Locks the room row until the current transaction ends, so bookings for the
     * same room are checked and inserted one at a time. Must run in a transaction.
     *
     * @return false if the room does not exist
     */
    public boolean lockForBooking(long id) {
        return jdbc.sql("SELECT id FROM room WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(Long.class)
                .optional()
                .isPresent();
    }

    public void insert(Room room) {
        jdbc.sql("INSERT INTO room (id, name, location, capacity, equipment) "
                        + "VALUES (:id, :name, :location, :capacity, :equipment)")
                .param("id", room.id())
                .param("name", room.name())
                .param("location", room.location())
                .param("capacity", room.capacity())
                .param("equipment", String.join(",", room.equipment()))
                .update();
    }

    public int count() {
        return jdbc.sql("SELECT COUNT(*) FROM room").query(Integer.class).single();
    }

    public void deleteAll() {
        jdbc.sql("DELETE FROM room").update();
    }

    private static Room mapRoom(ResultSet rs, int rowNum) throws SQLException {
        String equipment = rs.getString("equipment");
        return new Room(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getInt("capacity"),
                equipment.isBlank() ? List.of() : Arrays.asList(equipment.split(",")));
    }
}
