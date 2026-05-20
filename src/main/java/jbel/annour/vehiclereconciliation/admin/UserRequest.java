package jbel.annour.vehiclereconciliation.admin;

public record UserRequest(
        String fullName,
        String email,
        String password,
        String role,
        Boolean active
) {
}
