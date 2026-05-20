package jbel.annour.vehiclereconciliation.config;

import jbel.annour.vehiclereconciliation.entity.User;
import jbel.annour.vehiclereconciliation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@jbelannour.ma";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        User admin = userRepository.findByEmail(DEFAULT_ADMIN_EMAIL)
                .orElseGet(User::new);

        admin.setFullName("Administrateur Jbel Annour");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setRole("ADMIN");
        admin.setActive(true);

        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        }

        userRepository.save(admin);
    }
}
