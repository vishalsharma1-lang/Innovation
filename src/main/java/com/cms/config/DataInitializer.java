package com.cms.config;

import com.cms.entity.AdminUser;
import com.cms.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!adminUserRepository.existsByUsername("admin")) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@cms.com");
            admin.setFullName("Administrator");
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            adminUserRepository.save(admin);
            System.out.println("✅ Default admin user created: admin / admin123");
        }
    }
}
