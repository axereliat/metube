package org.metube.service;

import com.sparkpost.Client;
import com.sparkpost.exception.SparkPostException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
       // SimpleMailMessage message = new SimpleMailMessage();
       // message.setTo(to);
       // message.setSubject(subject);
       // message.setText(text);
       // this.emailSender.send(message);
        //7f128097f14c98f6b88f77f4dbd8aeb1cbae207f     e4eaaa3f98abd0bc0d357c1de076ce0f0a8ceba2
        //String API_KEY = "7f128097f14c98f6b88f77f4dbd8aeb1cbae207f";
        //Client client = new Client(API_KEY);
//
        //try {
        //    client.sendMessage(
        //            "axereliat@gmail.com",
        //            to,
        //            subject,
        //            text,
        //            "");
        //} catch (SparkPostException e) {
        //    e.printStackTrace();
        //}

        //Setting up configurations for the email connection to the Google SMTP server using TLS
        Properties props = new Properties();
        // props.put("mail.smtp.host", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        //Establishing a session with required user details
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("axereliat@gmail.com", "*******");
            }
        });
        try {
            //Creating a Message object to set the email content
            MimeMessage msg = new MimeMessage(session);
            /*Parsing the String with defualt delimiter as a comma by marking the boolean as true and storing the email
            addresses in an array of InternetAddress objects*/
            InternetAddress[] address = InternetAddress.parse(to, true);
            //Setting the recepients from the address variable
            msg.setRecipients(Message.RecipientType.TO, address);
            String timeStamp = new SimpleDateFormat("yyyymmdd_hh-mm-ss").format(new Date());
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(text);
            msg.setHeader("XPriority", "1");
            Transport.send(msg);
            System.out.println("Mail has been sent successfully");
        } catch (MessagingException mex) {
            System.out.println("Unable to send an email" + mex);
        }
    }
}
