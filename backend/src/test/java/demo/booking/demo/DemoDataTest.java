package demo.booking.demo;

import static org.assertj.core.api.Assertions.assertThat;

import demo.booking.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class DemoDataTest extends ApiTestSupport {

    @Test
    void resetRestoresFixturesAnchoredToTheVenueDateAndDiscardsNewBookings() {
        long added = post("/api/reservations", booking(AURORA, AVERY, "Temporary", "2026-11-10T10:00", "2026-11-10T11:00")).id();

        ApiResponse reset = post("/api/demo/reset", null);

        assertThat(reset.status()).isEqualTo(200);
        assertThat(reset.body().get("anchorDate").asString()).isEqualTo(TODAY.toString());
        assertThat(get("/api/reservations/" + added).status()).isEqualTo(404);
        assertThat(get("/api/rooms").body().size()).isEqualTo(5);
        assertThat(get("/api/people").body().size()).isEqualTo(5);
        JsonNode all = get("/api/reservations?view=all").body();
        assertThat(all.size()).isEqualTo(DemoFixtures.RESERVATIONS.size());
        // "Team stand-up" is fixed at 09:00 venue time on the day after the anchor date.
        assertThat(all.valueStream().filter(r -> r.get("title").asString().equals("Team stand-up"))
                .map(r -> r.get("start").asString())).containsExactly("2026-10-21T09:00:00+01:00");
    }

    @Test
    void startupSeedingLeavesExistingDataAlone() {
        long kept = post("/api/reservations", booking(AURORA, AVERY, "Keep me", "2026-11-10T10:00", "2026-11-10T11:00")).id();

        assertThat(demoData.seedIfEmpty()).isFalse();

        assertThat(get("/api/reservations/" + kept).status()).isEqualTo(200);
    }
}
