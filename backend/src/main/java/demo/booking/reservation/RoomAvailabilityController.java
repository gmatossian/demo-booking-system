package demo.booking.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only views of a room's bookings. Neither reserves anything. */
@RestController
@RequestMapping("/api/rooms/{roomId}")
class RoomAvailabilityController {

    private final ReservationService service;

    RoomAvailabilityController(ReservationService service) {
        this.service = service;
    }

    @GetMapping("/availability")
    AvailabilityResponse availability(
            @PathVariable long roomId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {
        List<ReservationResponse> conflicts = service.toResponses(service.findConflicts(roomId, start, end));
        return new AvailabilityResponse(conflicts.isEmpty(), conflicts);
    }

    @GetMapping("/schedule")
    ScheduleResponse schedule(
            @PathVariable long roomId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        return new ScheduleResponse(date, service.toResponses(service.schedule(roomId, date)));
    }

    record AvailabilityResponse(boolean available, List<ReservationResponse> conflicts) {
    }

    record ScheduleResponse(LocalDate date, List<ReservationResponse> reservations) {
    }
}
