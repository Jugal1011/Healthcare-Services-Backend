package com.hospital.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default, free, zero-dependency SMS "sender" used for local development and demos.
 * It just logs the OTP to the console instead of hitting a paid SMS gateway.
 * Enabled whenever twilio.enabled=false (the default).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "false", matchIfMissing = true)
public class LogSmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String mobileNumber, String message) {
        log.info("=== [DEV SMS] To: {} | Message: {} ===", mobileNumber, message);
    }
}
