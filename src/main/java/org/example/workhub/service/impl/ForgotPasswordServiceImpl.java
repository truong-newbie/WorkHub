package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.common.ChangePassword;
import org.example.workhub.domain.dto.common.MailBody;
import org.example.workhub.domain.dto.response.CommonResponseDto;
import org.example.workhub.domain.entity.ForgotPassword;
import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.BadRequestException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.ForgotPasswordRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.service.EmailService;
import org.example.workhub.service.ForgotPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    UserRepository userRepository;
    EmailService emailService;
    ForgotPasswordRepository forgotPasswordRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public CommonResponseDto verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL));

        int otp= otpGenerator();
        MailBody mailBody= MailBody.builder()
                .to(email)
                .text("This is OTP for your Forgot Password request :" + otp)
                .subject("OTP for Forgot Password request")
                .build();

        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70*1000))
                .user(user)
                .build();

        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(fp);
        return new CommonResponseDto(true, "Email sent for verification");

    }

    @Override
    public void verifyOtp(Integer otp, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new BadRequestException(ErrorMessage.INVALID_OTP));

        // check expire
        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getId());
            throw new BadRequestException(ErrorMessage.OTP_EXPIRED);
        }

        forgotPasswordRepository.save(fp);
    }

    @Override
    public void changePassword(ChangePassword changePassword , String email) {
        if(!Objects.equals(changePassword.password() , changePassword.repeatPassword())){
            throw new BadRequestException(ErrorMessage.INVALID_REPEAT_PASSWORD);
        }
        userRepository.updatePassword(email , passwordEncoder.encode(changePassword.password()));
    }

    private Integer otpGenerator(){
        Random random= new Random();
        return random.nextInt(100_000 , 999_999);
    }
}
