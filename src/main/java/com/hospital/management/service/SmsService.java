package com.hospital.management.service;

public interface SmsService {
    void sendSms(String mobileNumber, String message);
}
