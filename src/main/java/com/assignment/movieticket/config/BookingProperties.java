package com.assignment.movieticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    private long holdTtlSeconds = 300;
    private long holdSweepIntervalMs = 30000;
    private long reminderLeadHours = 2;
    private long reminderScanIntervalMs = 300000;

    public long getHoldTtlSeconds() { return holdTtlSeconds; }
    public void setHoldTtlSeconds(long holdTtlSeconds) { this.holdTtlSeconds = holdTtlSeconds; }

    public long getHoldSweepIntervalMs() { return holdSweepIntervalMs; }
    public void setHoldSweepIntervalMs(long holdSweepIntervalMs) { this.holdSweepIntervalMs = holdSweepIntervalMs; }

    public long getReminderLeadHours() { return reminderLeadHours; }
    public void setReminderLeadHours(long reminderLeadHours) { this.reminderLeadHours = reminderLeadHours; }

    public long getReminderScanIntervalMs() { return reminderScanIntervalMs; }
    public void setReminderScanIntervalMs(long reminderScanIntervalMs) { this.reminderScanIntervalMs = reminderScanIntervalMs; }
}
