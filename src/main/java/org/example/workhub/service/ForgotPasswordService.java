package org.example.workhub.service;

import org.example.workhub.domain.dto.common.ChangePassword;
import org.example.workhub.domain.dto.response.CommonResponseDto;

public interface ForgotPasswordService {
    CommonResponseDto verifyEmail(String email);

    void verifyOtp( Integer otp,  String email);

    void changePassword(ChangePassword changePassword, String email);
}
