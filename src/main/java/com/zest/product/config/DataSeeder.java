package com.zest.product.config;

import com.zest.product.entity.Role;
import com.zest.product.entity.User;
import com.zest.product.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Default admin user created: admin/admin123");
            }

            if (!userRepository.existsByUsername("user")) {
                User user = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user123"))
                        .roles(Set.of(Role.ROLE_USER))
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.info("Default user created: user/user123");
            }
        };
    }
}
