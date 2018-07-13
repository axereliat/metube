package org.metube.config;

import org.metube.interceptors.TitleModifierInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TitleModifierInterceptor titleModifyInterceptor;

    @Autowired
    public WebMvcConfig(TitleModifierInterceptor titleModifyInterceptor) {
        this.titleModifyInterceptor = titleModifyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(titleModifyInterceptor);
    }
}
