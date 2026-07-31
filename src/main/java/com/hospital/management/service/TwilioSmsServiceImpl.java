package com.hospital.management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Slf4j
@Service
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "true")
public class TwilioSmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Override
    public void sendSms(String mobileNumber, String message) {
        Twilio.init(accountSid, authToken);
        Message.creator(new PhoneNumber(mobileNumber), new PhoneNumber(fromNumber), message).create();
        log.info("Twilio SMS would be sent to {} — uncomment Twilio SDK code in TwilioSmsServiceImpl to activate", mobileNumber);
    }
}
