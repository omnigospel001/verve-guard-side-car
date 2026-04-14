package com.verve.guard.service;

import com.verve.guard.response.DeviceResponse;

public interface EmailService {
    void sendVerificationEmail(DeviceResponse deviceResponse);
}
