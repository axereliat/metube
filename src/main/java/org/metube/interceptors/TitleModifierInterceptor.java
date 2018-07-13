package org.metube.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TitleModifierInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) return;

        Object titleCandidate = modelAndView.getModel().get("title");
        if (titleCandidate == null) {
            modelAndView.getModel().put("title", "MeTube");
        } else {
            modelAndView.getModel().put("title", "MeTube - " + modelAndView.getModel().get("title").toString());
        }
    }
}
