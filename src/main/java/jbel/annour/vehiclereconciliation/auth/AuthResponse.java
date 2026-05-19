package jbel.annour.vehiclereconciliation.auth;

public record AuthResponse(
        String token,
        String fullName,
        String email,
        String role
) {
}
