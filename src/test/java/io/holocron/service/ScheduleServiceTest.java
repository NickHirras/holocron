package io.holocron.service;

import io.holocron.ceremony.Ceremony;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
public class ScheduleServiceTest {

    @Inject
    ScheduleService scheduleService;

    @Test
    public void testScheduleCeremony() {
        Ceremony ceremony = new Ceremony();
        ceremony.title = "Test Ceremony";
        ceremony.rrule = "FREQ=DAILY;COUNT=10";
        ceremony.timezone = "UTC";

        // Just verify no exception is thrown during "scheduling" attempt
        assertDoesNotThrow(() -> scheduleService.scheduleCeremony(ceremony));
    }
}
