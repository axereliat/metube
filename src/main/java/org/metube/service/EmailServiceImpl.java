package org.metube.service;

import com.sparkpost.Client;
import com.sparkpost.exception.SparkPostException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Autowired
    public EmailServiceImpl(@Qualifier("getJavaMailSender") JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        //SimpleMailMessage message = new SimpleMailMessage();
        //message.setTo(to);
        //message.setSubject(subject);
        //message.setText(text);
        //emailSender.send(message);
        //7f128097f14c98f6b88f77f4dbd8aeb1cbae207f
        String API_KEY = "e4eaaa3f98abd0bc0d357c1de076ce0f0a8ceba2";
        Client client = new Client(API_KEY);

        try {
            client.sendMessage(
                    "axereliat@gmail.com",
                    to,
                    subject,
                    text,
                    "");
        } catch (SparkPostException e) {
            e.printStackTrace();
        }
    }
}
