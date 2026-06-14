package com.fraudguard.service;

import com.fraudguard.model.Notification;
import java.util.List;

public interface NotificationService {
    Notification createNotification(String message, String targetRole);
    List<Notification> getNotificationsForUser(String username);
    long getUnreadCountForUser(String username);
    void markAsRead(Long id);
    void markAllAsReadForUser(String username);
}
