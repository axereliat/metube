package org.metube.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class BeanConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        //mailSender.setHost("smtp.gmail.com");
        //mailSender.setPort(587);
//
        //mailSender.setUsername("axereliatgmail.com");
        //mailSender.setPassword("mario789");
//
        //Properties props = mailSender.getJavaMailProperties();
        //props.put("mail.transport.protocol", "smtp");
        //props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.starttls.enable", "true");
        //props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "This is the test email template for your email:\n%s\n");
        return message;
    }
}
