package org.metube.config;

import org.metube.interceptor.TitleModifierInterceptor;
import org.metube.interceptor.UserAddInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TitleModifierInterceptor titleModifyInterceptor;

    private final UserAddInterceptor userAddInterceptor;

    @Autowired
    public WebMvcConfig(TitleModifierInterceptor titleModifyInterceptor, UserAddInterceptor userAddInterceptor) {
        this.titleModifyInterceptor = titleModifyInterceptor;
        this.userAddInterceptor = userAddInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(titleModifyInterceptor);
        registry.addInterceptor(userAddInterceptor);
    }
}
