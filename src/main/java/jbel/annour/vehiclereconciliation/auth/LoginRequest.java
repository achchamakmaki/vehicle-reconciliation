package jbel.annour.vehiclereconciliation.auth;

public record LoginRequest(
        String email,
        String password
) {
}
