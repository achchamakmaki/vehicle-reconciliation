package jbel.annour.vehiclereconciliation.auth;

public record CurrentUserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        Boolean active
) {
}
