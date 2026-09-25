package demo.booking.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DemoDataController {

    private final DemoDataService demoData;

    DemoDataController(DemoDataService demoData) {
        this.demoData = demoData;
    }

    /** Discards all bookings and restores the fictional fixtures. */
    @PostMapping("/api/demo/reset")
    DemoDataService.DemoDataSummary reset() {
        return demoData.reset();
    }
}
