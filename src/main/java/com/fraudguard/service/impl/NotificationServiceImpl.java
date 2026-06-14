package com.fraudguard.service.impl;

import com.fraudguard.model.Notification;
import com.fraudguard.model.Role;
import com.fraudguard.model.User;
import com.fraudguard.repository.NotificationRepository;
import com.fraudguard.repository.UserRepository;
import com.fraudguard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Notification createNotification(String message, String targetRole) {
        Notification notification = Notification.builder()
                .message(message)
                .targetRole(targetRole)
                .readStatus(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        User user = userOpt.get();
        List<Notification> list = new ArrayList<>();
        
        // Find notifications for user's roles
        for (Role role : user.getRoles()) {
            list.addAll(notificationRepository.findByTargetRoleOrderByCreatedAtDesc(role.getName()));
        }
        
        // Find general notifications
        list.addAll(notificationRepository.findByTargetRoleOrderByCreatedAtDesc("ALL"));
        
        // Sort list by createdAt descending
        list.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
        
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCountForUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return 0;
        }
        User user = userOpt.get();
        long unreadCount = 0;
        
        for (Role role : user.getRoles()) {
            unreadCount += notificationRepository.countUnreadByTargetRole(role.getName());
        }
        unreadCount += notificationRepository.countUnreadByTargetRole("ALL");
        
        return unreadCount;
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(String username) {
        List<Notification> notifications = getNotificationsForUser(username);
        for (Notification n : notifications) {
            if (!n.isReadStatus()) {
                n.setReadStatus(true);
                notificationRepository.save(n);
            }
        }
    }
}
