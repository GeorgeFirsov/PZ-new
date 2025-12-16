package ru.mtuci.coursemanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
                log.info("Создан администратор по умолчанию: admin / Admin123!");
            }

            if (userRepository.findByUsername("student").isEmpty()) {
                User student = new User();
                student.setUsername("student");
                student.setPassword(passwordEncoder.encode("Student123!"));
                student.setRole("STUDENT");
                userRepository.save(student);
                log.info("Создан тестовый студент: student / Student123!");
            }

            log.info("Инициализация базы данных завершена");
        };
    }
}