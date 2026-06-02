package com.shiksha.scheduler.service;

import com.shiksha.scheduler.model.Notification;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void send(User recipient, String message, String link) {
        Notification n = Notification.builder()
                .recipient(recipient)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getUnread(User user) {
        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
    }

    public List<Notification> getAll(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public long countUnread(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllRead(User user) {
        List<Notification> unread = getUnread(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
