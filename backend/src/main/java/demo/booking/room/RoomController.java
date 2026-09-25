package demo.booking.room;

import java.util.List;

import demo.booking.web.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
class RoomController {

    private final RoomRepository rooms;

    RoomController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @GetMapping
    List<Room> list() {
        return rooms.findAll();
    }

    @GetMapping("/{roomId}")
    Room get(@PathVariable long roomId) {
        return rooms.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " does not exist."));
    }
}
