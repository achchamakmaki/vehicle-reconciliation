package jbel.annour.vehiclereconciliation.admin;

import jbel.annour.vehiclereconciliation.entity.User;
import jbel.annour.vehiclereconciliation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Cet email est deja utilise");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(normalizeRole(request.role()));
        user.setActive(request.active() == null || request.active());
        return toResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findEntityById(id);

        userRepository.findByEmail(request.email())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("Cet email est deja utilise");
                });

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(normalizeRole(request.role()));
        user.setActive(request.active() == null || request.active());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(userRepository.save(user));
    }

    public UserResponse toggleStatus(Long id) {
        User user = findEntityById(id);
        user.setActive(!user.isEnabled());
        return toResponse(userRepository.save(user));
    }

    public UserResponse resetPassword(Long id, ResetPasswordRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire");
        }

        User user = findEntityById(id);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findEntityById(id);
        userRepository.delete(user);
        userRepository.flush();
    }

    private User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.normalizedRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        return role.replace("ROLE_", "").trim().toUpperCase();
    }
}
