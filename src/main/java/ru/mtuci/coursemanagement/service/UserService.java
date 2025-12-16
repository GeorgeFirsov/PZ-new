package ru.mtuci.coursemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }

        if (!isValidRole(user.getRole())) {
            user.setRole("STUDENT");
        }

        return repo.save(user);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void changeUserRole(Long userId, String newRole) {
        repo.findById(userId).ifPresent(user -> {
            if (isValidRole(newRole)) {
                user.setRole(newRole);
                repo.save(user);
            }
        });
    }

    private boolean isValidRole(String role) {
        if (role == null) return false;
        return role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN");
    }
}