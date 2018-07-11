package org.metube.service;

import org.metube.entity.Notification;

import java.util.List;

public interface NotificationService {

    void save(Notification notification);

    void deleteById(Integer id);

    List<Notification> findAll();

    Notification findById(Integer id);
}
