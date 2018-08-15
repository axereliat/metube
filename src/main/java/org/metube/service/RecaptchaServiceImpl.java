package org.metube.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${google.recaptcha.site-secret}")
    private String recaptchaSecret;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public RecaptchaServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public String verifyRecaptcha(String ip,
                                  String recaptchaResponse){
        Map<String, String> body = new HashMap<>();
        body.put("secret", recaptchaSecret);
        body.put("response", recaptchaResponse);
        body.put("remoteip", ip);

        ResponseEntity<Map> recaptchaResponseEntity =
                restTemplateBuilder.build()
                        .postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL+
                                        "?secret={secret}&response={response}&remoteip={remoteip}",
                                body, Map.class, body);

        Map<String, Object> responseBody =
                recaptchaResponseEntity.getBody();
        boolean recaptchaSucess = (Boolean)responseBody.get("success");
        if (!recaptchaSucess) {
            return "success";
        } else {
            return null;
        }
    }
}