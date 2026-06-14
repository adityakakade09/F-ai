package com.fraudguard.repository;

import com.fraudguard.model.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {
    Optional<LoginSession> findByToken(String token);
    List<LoginSession> findByUsernameAndLogoutTimeIsNull(String username);
    List<LoginSession> findByUsernameOrderByLoginTimeDesc(String username);
}
