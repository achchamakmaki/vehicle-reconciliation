package jbel.annour.vehiclereconciliation.util;

public class MatriculeUtils {

    public static String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value.trim().replace(" ", "");

        String[] parts = cleaned.split("-");

        if (parts.length == 3) {

            String first = parts[0];
            String second = parts[1];
            String third = parts[2];

            // toujours retourner format unique : numero + lettre + ville
            return first + second + third;
        }

        return cleaned.replace("-", "");
    }
}