package com.greenride.service.port;

public interface SmsNotificationPort {
    void sendSms(String phoneNumber, String message);

    // Νέα μέθοδος για validation
    boolean validatePhoneNumber(String phoneNumber);
}