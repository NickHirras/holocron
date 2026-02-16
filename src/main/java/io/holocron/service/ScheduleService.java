package io.holocron.service;

import io.holocron.ceremony.Ceremony;
import io.holocron.pulse.PulseService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.scheduler.ScheduledExecution;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class ScheduleService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);

    @Inject
    Scheduler scheduler;

    @Inject
    PulseService pulseService;

    // Run on startup to schedule all active ceremonies
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Protocol Droids initializing... scanning for ceremonies.");
        scheduleAll();
    }

    public void scheduleAll() {
        // Find all active ceremonies that have an RRule
        // Note: We need to filter in memory or add a query method
        List<Ceremony> ceremonies = Ceremony.listAll();
        for (Ceremony ceremony : ceremonies) {
            if (ceremony.isActive && ceremony.rrule != null && !ceremony.rrule.isEmpty()) {
                scheduleCeremony(ceremony);
            }
        }
    }

    public void scheduleCeremony(Ceremony ceremony) {
        try {
            // 1. Parse RRule
            RecurrenceRule rule = new RecurrenceRule(ceremony.rrule);

            // 2. Calculate next occurrence
            // Logic: Get next instance from NOW
            // For MVP, using default system time. In production, respect ceremony.timezone.
            org.dmfs.rfc5545.DateTime start = org.dmfs.rfc5545.DateTime.now();

            RecurrenceRuleIterator it = rule.iterator(start);

            if (it.hasNext()) {
                org.dmfs.rfc5545.DateTime next = it.nextDateTime();
                long nextMillis = next.getTimestamp();

                // If "next" is now or in past, skip to next
                if (nextMillis <= System.currentTimeMillis()) {
                    if (it.hasNext()) {
                        next = it.nextDateTime();
                        nextMillis = next.getTimestamp();
                    }
                }

                long delay = nextMillis - System.currentTimeMillis();

                if (delay > 0) {
                    LOG.info("Scheduling ceremony '{}' for {} (in {} ms)", ceremony.title, next, delay);

                    // 3. Schedule the delayed job
                    String delayStr = java.time.Duration.ofMillis(delay).toString();
                    scheduler.newJob("pulse_" + ceremony.id)
                            .setDelayed(delayStr)
                            .setTask(execution -> triggerPulse(ceremony.id))
                            .schedule();
                } else {
                    LOG.warn("Could not find future occurrence for ceremony {}", ceremony.id);
                }
            }

        } catch (Exception e) {
            LOG.error("Failed to schedule ceremony " + ceremony.id, e);
        }
    }

    @Transactional
    public void triggerPulse(Long ceremonyId) {
        LOG.info("Protocol Droid triggering pulse for ceremony {}", ceremonyId);
        // Reschedule next
        Ceremony c = Ceremony.findById(ceremonyId);
        if (c != null && c.isActive) {
            // TODO: Call PulseService to create the actual pulse record
            // pulseService.createPulse(c);

            // Re-schedule the next one
            scheduleCeremony(c);
        }
    }

    // Example scheduled method for testing general scheduler presence
    @Scheduled(every = "1h")
    void heartbeat() {
        LOG.debug("Protocol Droid Heartbeat");
    }
}
