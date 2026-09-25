package demo.booking.reservation;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
class ReservationController {

    private final ReservationService service;

    ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    List<ReservationResponse> list(@RequestParam(defaultValue = "upcoming") String view) {
        return service.toResponses(service.list(ReservationListView.parse(view)));
    }

    @GetMapping("/{id}")
    ReservationResponse get(@PathVariable long id) {
        return service.toResponse(service.get(id));
    }

    @PostMapping
    ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse created = service.toResponse(service.create(request));
        return ResponseEntity.created(URI.create("/api/reservations/" + created.id())).body(created);
    }

    /** Idempotent for already-cancelled reservations: returns the unchanged record. */
    @PostMapping("/{id}/cancel")
    ReservationResponse cancel(@PathVariable long id) {
        return service.toResponse(service.cancel(id));
    }
}
