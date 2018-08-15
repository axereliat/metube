package org.metube.controller;

import org.metube.entity.Notification;
import org.metube.entity.User;
import org.metube.service.NotificationService;
import org.metube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String list(Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userService.findByUsername(user.getUsername());

        List<Notification> notifications = new ArrayList<>(userEntity.getNotifications());

        model.addAttribute("title", "List notifications");
        model.addAttribute("notifications", Arrays.asList(notifications.stream().sorted((x1, x2) -> x2.getAddedOn().compareTo(x1.getAddedOn())).toArray(Notification[]::new)));
        model.addAttribute("view", "notification/list");


        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/delete/{id}", produces = "application/json")
    public @ResponseBody Map<String, String> delete(@PathVariable Integer id) {

        this.notificationService.deleteById(id);
        Map<String, String> map = new HashMap<>();
        map.put("id", id.toString());
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userService.findByUsername(user.getUsername());

        map.put("notificationCount", String.valueOf(userEntity.getNotifications().size()));
        return map;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/clear")
    public String clear() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userService.findByUsername(user.getUsername());

        for (Notification notification : userEntity.getNotifications()) {
            this.notificationService.deleteById(notification.getId());
        }

        return "redirect:/notifications/";
    }
}
