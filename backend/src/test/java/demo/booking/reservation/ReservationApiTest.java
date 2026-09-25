package demo.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import demo.booking.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** Booking rules exercised through the real HTTP API and database. */
class ReservationApiTest extends ApiTestSupport {

    private static final String DAY = "2026-11-10";

    @Test
    void createdReservationAppearsInScheduleListAndDetail() {
        ApiResponse created = post("/api/reservations",
                booking(AURORA, AVERY, "  Budget review  ", DAY + "T10:00", DAY + "T11:00"));

        assertThat(created.status()).isEqualTo(201);
        assertThat(created.body().get("title").asString()).isEqualTo("Budget review");
        assertThat(created.body().get("status").asString()).isEqualTo("ACTIVE");
        assertThat(created.body().get("start").asString()).isEqualTo(DAY + "T10:00:00Z");

        assertThat(ids(get("/api/rooms/1/schedule?date=" + DAY).body().get("reservations")))
                .containsExactly(created.id());
        assertThat(ids(get("/api/reservations?view=upcoming").body())).contains(created.id());
        assertThat(get("/api/reservations/" + created.id()).body().get("personName").asString())
                .isEqualTo("Avery Quinn");
    }

    @Test
    void overlappingActiveReservationIsRejectedWithConflictDetails() {
        long existing = post("/api/reservations",
                booking(AURORA, AVERY, "Existing", DAY + "T10:00", DAY + "T11:00")).id();

        ApiResponse overlap = post("/api/reservations",
                booking(AURORA, JORDAN, "Overlap", DAY + "T10:45", DAY + "T11:30"));

        assertThat(overlap.status()).isEqualTo(409);
        assertThat(overlap.code()).isEqualTo("reservation_conflict");
        assertThat(ids(overlap.body().get("conflicts"))).containsExactly(existing);
        assertThat(ids(get("/api/rooms/1/schedule?date=" + DAY).body().get("reservations")))
                .containsExactly(existing);
    }

    @Test
    void intervalsAreHalfOpenAndConflictsArePerRoom() {
        assertThat(post("/api/reservations", booking(AURORA, AVERY, "Middle", DAY + "T10:00", DAY + "T11:00"))
                .status()).isEqualTo(201);

        // Touching at either boundary is allowed.
        assertThat(post("/api/reservations", booking(AURORA, JORDAN, "Before", DAY + "T09:00", DAY + "T10:00"))
                .status()).isEqualTo(201);
        assertThat(post("/api/reservations", booking(AURORA, JORDAN, "After", DAY + "T11:00", DAY + "T12:00"))
                .status()).isEqualTo(201);
        // Enclosing an existing reservation is not.
        assertThat(post("/api/reservations", booking(AURORA, JORDAN, "Enclosing", DAY + "T08:00", DAY + "T13:00"))
                .status()).isEqualTo(409);
        // The same time in another room is fine, even for the same person.
        assertThat(post("/api/reservations", booking(BEACON, AVERY, "Other room", DAY + "T10:00", DAY + "T11:00"))
                .status()).isEqualTo(201);
    }

    @Test
    void cancellationReleasesTheSlotAndKeepsHistory() {
        long first = post("/api/reservations", booking(AURORA, AVERY, "First", DAY + "T10:00", DAY + "T11:00")).id();

        ApiResponse cancelled = post("/api/reservations/" + first + "/cancel", null);
        assertThat(cancelled.status()).isEqualTo(200);
        assertThat(cancelled.body().get("status").asString()).isEqualTo("CANCELLED");
        assertThat(cancelled.body().get("cancelledAt").isNull()).isFalse();

        assertThat(get("/api/rooms/1/availability?start=" + DAY + "T10:00&end=" + DAY + "T11:00")
                .body().get("available").asBoolean()).isTrue();
        assertThat(post("/api/reservations", booking(AURORA, JORDAN, "Second", DAY + "T10:00", DAY + "T11:00"))
                .status()).isEqualTo(201);
        assertThat(ids(get("/api/reservations?view=cancelled").body())).contains(first);
    }

    @Test
    void repeatedCancellationReturnsTheUnchangedRecordEvenAfterTheStartTime() {
        long id = post("/api/reservations", booking(BEACON, AVERY, "Plan", "2026-10-21T10:00", "2026-10-21T11:00")).id();
        JsonNode firstCancel = post("/api/reservations/" + id + "/cancel", null).body();

        clock.advance(Duration.ofMinutes(5));
        ApiResponse repeat = post("/api/reservations/" + id + "/cancel", null);
        assertThat(repeat.status()).isEqualTo(200);
        assertThat(repeat.body().get("cancelledAt")).isEqualTo(firstCancel.get("cancelledAt"));

        clock.advance(Duration.ofDays(2)); // now after the reservation's start and end
        ApiResponse late = post("/api/reservations/" + id + "/cancel", null);
        assertThat(late.status()).isEqualTo(200);
        assertThat(late.body().get("status").asString()).isEqualTo("CANCELLED");
        assertThat(late.body().get("cancelledAt")).isEqualTo(firstCancel.get("cancelledAt"));
    }

    @Test
    void activeReservationCannotBeCancelledOnceStarted() {
        long id = post("/api/reservations", booking(AURORA, AVERY, "Soon", "2026-10-20T10:30", "2026-10-20T11:30")).id();
        clock.advance(Duration.ofMinutes(30)); // 10:30 venue time: the reservation has started

        ApiResponse response = post("/api/reservations/" + id + "/cancel", null);

        assertThat(response.status()).isEqualTo(409);
        assertThat(response.code()).isEqualTo("cancellation_not_allowed");
        assertThat(get("/api/reservations/" + id).body().get("status").asString()).isEqualTo("ACTIVE");
    }

    @Test
    void invalidRequestsReportFieldErrorsAndChangeNothing() {
        int before = get("/api/reservations?view=all").body().size();

        ApiResponse missing = post("/api/reservations", Map.of("title", " "));
        assertThat(missing.status()).isEqualTo(400);
        assertThat(missing.code()).isEqualTo("validation_failed");
        assertThat(fields(missing)).contains("roomId", "personId", "title", "start", "end");

        ApiResponse unknown = post("/api/reservations", booking(99, 99, "Ghost", DAY + "T10:00", DAY + "T11:00"));
        assertThat(unknown.status()).isEqualTo(400);
        assertThat(fields(unknown)).containsExactlyInAnyOrder("roomId", "personId");

        ApiResponse badTimes = post("/api/reservations", booking(AURORA, AVERY, "Bad", DAY + "T11:00", DAY + "T10:00"));
        assertThat(badTimes.status()).isEqualTo(400);
        assertThat(fields(badTimes)).contains("end");

        ApiResponse past = post("/api/reservations", booking(AURORA, AVERY, "Past", "2026-10-19T10:00", "2026-10-19T11:00"));
        assertThat(fields(past)).contains("start");

        assertThat(get("/api/reservations?view=all").body().size()).isEqualTo(before);
    }

    @Test
    void missingRecordsReturnNotFound() {
        assertThat(get("/api/reservations/999999").status()).isEqualTo(404);
        assertThat(post("/api/reservations/999999/cancel", null).status()).isEqualTo(404);
        assertThat(get("/api/rooms/999/schedule?date=" + DAY).status()).isEqualTo(404);
        assertThat(get("/api/reservations/999999").code()).isEqualTo("not_found");
    }

    @Test
    void responsesCarryTheVenueOffsetAcrossTheClockChange() {
        ApiResponse summerTime = post("/api/reservations",
                booking(AURORA, AVERY, "Before change", "2026-10-24T09:00", "2026-10-24T10:00"));
        ApiResponse winterTime = post("/api/reservations",
                booking(AURORA, AVERY, "After change", "2026-10-25T09:00", "2026-10-25T10:00"));

        assertThat(OffsetDateTime.parse(summerTime.body().get("start").asString()).getOffset())
                .isEqualTo(ZoneOffset.ofHours(1));
        assertThat(OffsetDateTime.parse(winterTime.body().get("start").asString()).getOffset())
                .isEqualTo(ZoneOffset.UTC);
    }

    private static List<Long> ids(JsonNode reservations) {
        return reservations.valueStream().map(r -> r.get("id").asLong()).toList();
    }

    private static List<String> fields(ApiResponse response) {
        return response.body().get("errors").valueStream().map(e -> e.get("field").asString()).toList();
    }
}
