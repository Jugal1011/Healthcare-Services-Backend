package com.hospital.management.service;

import com.hospital.management.entity.PasswordResetOtp;
import com.hospital.management.exception.BadRequestException;
import com.hospital.management.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final PasswordResetOtpRepository otpRepository;
    private final SmsService smsService;

    @Value("${app.otp.expiry-minutes}")
    private int expiryMinutes;

    @Value("${app.otp.length}")
    private int otpLength;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String mobileNumber) {
        String otp = generateNumericOtp(otpLength);

        PasswordResetOtp entity = PasswordResetOtp.builder()
                .mobileNumber(mobileNumber)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();
        otpRepository.save(entity);

        smsService.sendSms(mobileNumber, "Your password reset OTP is " + otp +
                ". It expires in " + expiryMinutes + " minutes.");
    }

    @Transactional
    public void validateOtp(String mobileNumber, String otp) {
        PasswordResetOtp record = otpRepository
                .findTopByMobileNumberAndUsedFalseOrderByCreatedAtDesc(mobileNumber)
                .orElseThrow(() -> new BadRequestException("No OTP request found for this mobile number"));

        if (record.isUsed()) {
            throw new BadRequestException("OTP already used. Please request a new one");
        }
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one");
        }
        if (!record.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
