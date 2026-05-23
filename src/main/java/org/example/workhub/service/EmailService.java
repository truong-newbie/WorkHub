package org.example.workhub.service;


import org.example.workhub.domain.dto.common.MailBody;

public interface EmailService {
    void sendSimpleMessage(MailBody mailBody);

}
