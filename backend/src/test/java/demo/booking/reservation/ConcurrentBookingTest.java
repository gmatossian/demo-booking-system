package demo.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import demo.booking.support.ApiTestSupport;
import org.junit.jupiter.api.Test;

/**
 * Competing requests for overlapping times in one room, sent simultaneously over
 * HTTP to the real service and H2 database. Exactly one may win; every other
 * request must be told explicitly that it conflicts (not fail for another reason).
 */
class ConcurrentBookingTest extends ApiTestSupport {

    private static final int COMPETING_REQUESTS = 10;
    private static final int ROUNDS = 5;

    @Test
    void exactlyOneOfManySimultaneousOverlappingBookingsSucceeds() throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(COMPETING_REQUESTS)) {
            for (int round = 0; round < ROUNDS; round++) {
                String day = TODAY.plusDays(20 + round).toString();
                List<ApiResponse> responses = sendSimultaneously(pool, day);

                List<ApiResponse> created = responses.stream().filter(r -> r.status() == 201).toList();
                List<ApiResponse> others = responses.stream().filter(r -> r.status() != 201).toList();

                assertThat(created).as("successful bookings on %s", day).hasSize(1);
                long winner = created.getFirst().id();
                assertThat(others).as("losing responses on %s", day).hasSize(COMPETING_REQUESTS - 1)
                        .allSatisfy(response -> {
                            assertThat(response.status()).isEqualTo(409);
                            assertThat(response.code()).isEqualTo("reservation_conflict");
                            assertThat(response.body().get("conflicts").valueStream()
                                    .map(c -> c.get("id").asLong())).containsExactly(winner);
                        });

                assertThat(get("/api/rooms/" + AURORA + "/schedule?date=" + day)
                        .body().get("reservations").size()).isEqualTo(1);
            }
        }
    }

    /** Every request waits at a barrier, then all are sent at once. */
    private List<ApiResponse> sendSimultaneously(ExecutorService pool, String day) throws Exception {
        CyclicBarrier startTogether = new CyclicBarrier(COMPETING_REQUESTS);
        List<Future<ApiResponse>> futures = new ArrayList<>();
        for (int i = 0; i < COMPETING_REQUESTS; i++) {
            // Two different ranges that overlap each other (10:30–11:00 is shared).
            boolean early = i % 2 == 0;
            HttpRequest request = postRequest("/api/reservations", booking(AURORA, 1 + i % 5, "Contender " + i,
                    day + (early ? "T10:00" : "T10:30"), day + (early ? "T11:00" : "T11:30")));
            futures.add(pool.submit(() -> {
                startTogether.await();
                return send(request);
            }));
        }
        List<ApiResponse> responses = new ArrayList<>();
        for (Future<ApiResponse> future : futures) {
            responses.add(future.get());
        }
        return responses;
    }
}
