package org.metube.service;

public interface RecaptchaService {

    String verifyRecaptcha(String ip, String recaptchaResponse);
}
