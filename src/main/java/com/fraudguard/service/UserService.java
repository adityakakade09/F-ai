package com.fraudguard.service;

import com.fraudguard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
    User createUser(User user, List<String> roleNames);
    User updateUser(Long id, User userDetails, List<String> roleNames);
    void deleteUser(Long id);
    User getUserById(Long id);
    User getUserByUsername(String username);
    Page<User> getAllUsers(Pageable pageable);
    void setUserActiveStatus(Long id, boolean active);
    void setUserLockStatus(Long id, boolean locked);
}
