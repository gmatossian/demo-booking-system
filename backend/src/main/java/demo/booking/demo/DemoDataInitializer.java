package demo.booking.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** On startup, seeds an empty database. Never touches existing data. */
@Component
class DemoDataInitializer implements ApplicationRunner {

    private final DemoDataService demoData;

    DemoDataInitializer(DemoDataService demoData) {
        this.demoData = demoData;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoData.seedIfEmpty();
    }
}
