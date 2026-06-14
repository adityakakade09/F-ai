package com.fraudguard.service.impl;

import com.fraudguard.model.Role;
import com.fraudguard.model.User;
import com.fraudguard.repository.RoleRepository;
import com.fraudguard.repository.UserRepository;
import com.fraudguard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user, List<String> roleNames) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        Set<Role> roles = new HashSet<>();
        for (String rName : roleNames) {
            roleRepository.findByName(rName).ifPresent(roles::add);
        }
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails, List<String> roleNames) {
        User user = getUserById(id);
        
        user.setFullName(userDetails.getFullName());
        
        // If email changed, check existence
        if (!user.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        // If password is provided, encode and update
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Roles update
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String rName : roleNames) {
                roleRepository.findByName(rName).ifPresent(roles::add);
            }
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void setUserActiveStatus(Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void setUserLockStatus(Long id, boolean locked) {
        User user = getUserById(id);
        user.setLocked(locked);
        if (!locked) {
            user.setFailedLoginAttempts(0); // Reset attempts on unlock
        }
        userRepository.save(user);
    }
}
