package jbel.annour.vehiclereconciliation.vehicle;

public final class VehicleTypeMapper {

    private VehicleTypeMapper() {
    }

    public static String toBusinessLabel(String type) {
        if (type == null || type.isBlank()) {
            return "Non defini";
        }

        String normalizedType = type.trim();
        return switch (normalizedType) {
            case "0", "1234567" -> "Non defini";
            case "1" -> "Camion grand tonnage";
            case "2" -> "Camionnette / VUL";
            case "3" -> "Petit camion benne";
            case "4" -> "Tractopelle (Trax)";
            case "5" -> "Chargeuse sur pneus (Loader)";
            case "6" -> "Chariot elevateur (Clark)";
            case "7" -> "Vehicule de service";
            default -> normalizedType.matches("\\d+") ? "Non defini" : normalizedType;
        };
    }

    public static boolean isSageTypeCode(String type) {
        if (type == null) {
            return false;
        }

        String normalizedType = type.trim();
        return normalizedType.matches("[0-7]") || "1234567".equals(normalizedType);
    }
}
