package org.metube.service;

import org.metube.domain.entity.Notification;
import org.metube.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void save(Notification notification) {
        this.notificationRepository.saveAndFlush(notification);
    }

    @Override
    public void deleteById(Integer id) {
        this.notificationRepository.deleteById(id);
    }

    @Override
    public List<Notification> findAll() {
        return this.notificationRepository.findAll();
    }

    @Override
    public Notification findById(Integer id) {
        return this.notificationRepository.findById(id).get();
    }
}
