package org.example.workhub.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.common.ChangePassword;
import org.example.workhub.domain.dto.request.VerifyOtpRequestDto;
import org.example.workhub.service.ForgotPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class ForgotPasswordController {
    ForgotPasswordService forgotPasswordService;


    //send mail for vertification mail
    @PostMapping(UrlConstant.ForgotPassword.VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@PathVariable String email) {
        return VsResponseUtil.success(forgotPasswordService.verifyEmail(email));
    }

    @PostMapping(UrlConstant.ForgotPassword.VERIFY_OTP)
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequestDto req) {
        forgotPasswordService.verifyOtp(req.getOtp(), req.getEmail());
        return VsResponseUtil.success("OTP verified successfully");
    }

    @PostMapping(UrlConstant.ForgotPassword.RESET_PASSWORD)
    public ResponseEntity<?> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                   @PathVariable String email) {
        forgotPasswordService.changePassword(changePassword, email);
        return VsResponseUtil.success("Password changed successfully");
    }
}
