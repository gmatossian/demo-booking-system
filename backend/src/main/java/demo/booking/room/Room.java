package demo.booking.room;

import java.util.List;

/** A fictional meeting room. Capacity and equipment are descriptive only. */
public record Room(long id, String name, String location, int capacity, List<String> equipment) {
}
