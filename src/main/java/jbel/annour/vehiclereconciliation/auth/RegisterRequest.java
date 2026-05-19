package jbel.annour.vehiclereconciliation.auth;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String role
) {
}
