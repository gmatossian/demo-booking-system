package demo.booking.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import demo.booking.demo.DemoDataService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs the real application on a random port with a disposable H2 file database
 * and a controllable clock. Each test starts from freshly reset demo data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestClockConfig.class)
public abstract class ApiTestSupport {

    /** Tuesday 20 October 2026, 10:00 in Europe/London (BST). */
    protected static final Instant NOW = Instant.parse("2026-10-20T09:00:00Z");
    protected static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    /** Room IDs from the demo fixtures. */
    protected static final long AURORA = 1;
    protected static final long BEACON = 2;
    protected static final long AVERY = 1;
    protected static final long JORDAN = 2;

    @LocalServerPort
    private int port;

    @Autowired
    protected MutableClock clock;

    @Autowired
    protected DemoDataService demoData;

    @Autowired
    protected JsonMapper json;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void resetState() {
        clock.set(NOW);
        demoData.reset();
    }

    protected ApiResponse get(String path) {
        return send(HttpRequest.newBuilder(uri(path)).GET().build());
    }

    protected ApiResponse post(String path, Object body) {
        return send(postRequest(path, body));
    }

    protected HttpRequest postRequest(String path, Object body) {
        return HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : json.writeValueAsString(body)))
                .build();
    }

    protected ApiResponse send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body().isEmpty() ? null : json.readTree(response.body());
            return new ApiResponse(response.statusCode(), body);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** A booking request body; times are venue-local, e.g. "2026-11-10T10:00". */
    protected static Map<String, Object> booking(long roomId, long personId, String title, String start, String end) {
        return Map.of("roomId", roomId, "personId", personId, "title", title, "start", start, "end", end);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    public record ApiResponse(int status, JsonNode body) {

        public String code() {
            return body.path("code").asString();
        }

        public long id() {
            return body.get("id").asLong();
        }
    }
}
