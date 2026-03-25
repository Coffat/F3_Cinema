package com.f3cinema.app.config;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.entity.enums.UserRole;
import com.f3cinema.app.repository.UserRepository;
import com.f3cinema.app.util.PasswordUtil;
import lombok.extern.log4j.Log4j2;

/**
 * Seeder to initialize default system data (e.g., Admin account).
 */
@Log4j2
public class DatabaseSeeder {

    private static final UserRepository userRepository = new UserRepository();

    public static void seed() {
        try {
            // Check if admin user exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(PasswordUtil.hash("1"))
                        .fullName("System Administrator")
                        .role(UserRole.ADMIN)
                        .build();
                
                userRepository.save(admin);
                log.info("Admin account seeded successfully: admin / 1");
            } else {
                log.info("Admin account already exists, skipping admin seeding.");
            }

            // Check if staff user exists
            if (userRepository.findByUsername("staff").isEmpty()) {
                User staff = User.builder()
                        .username("staff")
                        .password(PasswordUtil.hash("1"))
                        .fullName("Cinema Staff 01")
                        .role(UserRole.STAFF)
                        .build();
                
                userRepository.save(staff);
                log.info("Staff account seeded successfully: staff / 1");
            } else {
                log.info("Staff account already exists, skipping staff seeding.");
            }
        } catch (Exception e) {
            log.error("Failed to seed database.", e);
        }
    }
}
