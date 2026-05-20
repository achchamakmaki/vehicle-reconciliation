package jbel.annour.vehiclereconciliation.admin;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        Boolean active,
        LocalDateTime createdAt
) {
}
