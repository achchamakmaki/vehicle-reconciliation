package jbel.annour.vehiclereconciliation.util;

public class MatriculeUtils {

    public static String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value.trim().replace(" ", "");

        String[] parts = cleaned.split("-");

        if (parts.length == 3) {

            String number = parts[0];
            String second = parts[1];
            String third = parts[2];
            String letter = containsArabicLetter(second) ? second : third;
            String prefecture = containsArabicLetter(second) ? third : second;

            // Toujours retourner un format unique : numero + lettre arabe + prefecture.
            return number + letter + prefecture;
        }

        return cleaned.replace("-", "");
    }

    private static boolean containsArabicLetter(String value) {
        if (value == null) {
            return false;
        }

        return value.codePoints()
                .anyMatch(codePoint -> codePoint >= 0x0600 && codePoint <= 0x06FF);
    }
}
