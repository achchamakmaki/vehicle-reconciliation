package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SageX3VehicleService {

    private final ObjectMapper objectMapper;

    @Value("${application.sage-x3.vehicles-url}")
    private String vehiclesUrl;

    @Value("${application.sage-x3.authorization:}")
    private String authorization;

    @Value("${application.sage-x3.username:}")
    private String username;

    @Value("${application.sage-x3.password:}")
    private String password;

    public JsonNode fetchVehicles() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(vehiclesUrl))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .header("Accept", "application/json");

        String authorizationHeader = authorizationHeader();
        if (authorizationHeader != null) {
            requestBuilder.header("Authorization", authorizationHeader);
        }

        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Sage X3 a retourne le statut HTTP "
                        + response.statusCode()
                        + " : "
                        + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire la reponse Sage X3", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Appel Sage X3 interrompu", e);
        }
    }

    public List<Vehicle> fetchVehicleList() {
        JsonNode response = fetchVehicles();
        JsonNode resources = findVehiclesArray(response);
        List<Vehicle> vehicles = new ArrayList<>();

        if (resources == null || !resources.isArray()) {
            return vehicles;
        }

        for (JsonNode item : resources) {
            Vehicle vehicle = new Vehicle();
            vehicle.setSageCode(firstText(item, "sageCode", "code", "num", "numero", "dossier", "numeroDossier", "YNUM", "YVEH", "VHNUM", "VCRNUM"));
            vehicle.setMatricule(firstText(item, "matricule", "immatriculation", "noImmatriculation", "numeroEnregistrement", "registration", "REGNUM", "YIMMAT", "YIMM", "YREGNUM"));
            vehicle.setMarque(firstText(item, "marque", "brand", "make", "YMARQUE", "ZMARQUE", "BPRNAM"));
            vehicle.setModele(firstText(item, "modele", "model", "YMODELE", "ZMODELE", "MDL"));
            vehicle.setType(VehicleTypeMapper.toBusinessLabel(firstText(item, "type", "genre", "categorie", "YTYPE_0", "YTYP", "YTYPE", "TYP")));
            vehicle.setStatus("SAGE_X3");
            vehicle.setSource("SAGE_X3");

            if (vehicle.getMatricule() != null && !vehicle.getMatricule().isBlank()) {
                vehicles.add(vehicle);
            }
        }

        return vehicles;
    }

    private JsonNode findVehiclesArray(JsonNode response) {
        if (response == null) {
            return null;
        }

        if (response.isArray()) {
            return response;
        }

        for (String fieldName : List.of("$resources", "resources", "value", "items", "data", "results")) {
            JsonNode node = response.get(fieldName);
            if (node != null && node.isArray()) {
                return node;
            }
        }

        return null;
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = textByName(node, fieldName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String textByName(JsonNode node, String expectedName) {
        if (node == null || expectedName == null) {
            return null;
        }

        String expected = normalize(expectedName);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (normalize(field.getKey()).equals(expected)) {
                JsonNode value = field.getValue();
                return value == null || value.isNull() ? null : value.asText();
            }
        }

        return null;
    }

    private String normalize(String value) {
        return value.replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .replace("$", "")
                .toLowerCase(Locale.ROOT);
    }

    private String authorizationHeader() {
        if (authorization != null && !authorization.isBlank()) {
            return authorization.trim();
        }

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            String credentials = username.trim() + ":" + password;
            return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }

        return null;
    }
}
