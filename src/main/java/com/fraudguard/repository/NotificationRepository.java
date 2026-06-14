package com.fraudguard.repository;

import com.fraudguard.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE (n.targetRole = :role OR n.targetRole = 'ALL') ORDER BY n.createdAt DESC")
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(String role);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.targetRole = :role OR n.targetRole = 'ALL') AND n.readStatus = false")
    long countUnreadByTargetRole(String role);
}
