package org.example.workhub.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.workhub.domain.dto.common.MailBody;
import org.example.workhub.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level= AccessLevel.PRIVATE , makeFinal = true)
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

//    JavaMailSender javaMailSender;
    JavaMailSender javaMailSender;

    @Override
    public void sendSimpleMessage(MailBody mailBody) {
        SimpleMailMessage message= new SimpleMailMessage();

        message.setTo(mailBody.to());
        message.setFrom("dangtruong3122005@gmail.com");
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());


        javaMailSender.send(message);
    }
}
