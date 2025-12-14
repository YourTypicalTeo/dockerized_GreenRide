package com.greenride.dto;

public record PhoneNumberValidationResult(
        String raw,
        boolean valid,
        String type,
        String e164
) {}