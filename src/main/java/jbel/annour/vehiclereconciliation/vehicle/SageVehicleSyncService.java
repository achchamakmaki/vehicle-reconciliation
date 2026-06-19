package jbel.annour.vehiclereconciliation.vehicle;

import jbel.annour.vehiclereconciliation.infraction.InfractionService;
import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SageVehicleSyncService {

    private final VehicleRepository vehicleRepository;
    private final InfractionService infractionService;
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${application.sage-x3.vehicles-url}")
    private String vehiclesUrl;

    @Value("${application.sage-x3.authorization:}")
    private String authorization;

    @Value("${application.sage-x3.username:}")
    private String username;

    @Value("${application.sage-x3.password:}")
    private String password;

    @Transactional
    public SageVehicleSyncSummary syncVehicles() {
        RestTemplate restTemplate = buildSageRestTemplate();
        URI currentUri = URI.create(vehiclesUrl);
        int totalFetched = 0;
        int created = 0;
        int updated = 0;

        while (currentUri != null) {
            log.debug("Sage X3 vehicles sync - fetch page: {}", currentUri);
            SageVehicleResponse response = fetchPage(restTemplate, currentUri);
            List<SageVehicleResource> resources = response.resources() != null ? response.resources() : List.of();
            log.debug("Sage X3 vehicles sync - resources received: {}", resources.size());

            for (SageVehicleResource resource : resources) {
                totalFetched++;
                VehicleSyncResult result = createOrUpdateVehicle(resource);

                if (result == VehicleSyncResult.CREATED) {
                    created++;
                } else if (result == VehicleSyncResult.UPDATED) {
                    updated++;
                }
            }

            currentUri = nextUri(currentUri, response);
        }

        log.info("Sage X3 vehicles sync finished: totalFetched={}, created={}, updated={}", totalFetched, created, updated);
        return new SageVehicleSyncSummary(totalFetched, created, updated);
    }

    private SageVehicleResponse fetchPage(RestTemplate restTemplate, URI uri) {
        try {
            ResponseEntity<SageVehicleResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers()),
                    SageVehicleResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Sage X3 a retourne une reponse invalide pour " + uri);
            }

            return response.getBody();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Sage X3 a refuse la requete HTTP "
                            + e.getStatusCode().value()
                            + ". Verifiez les identifiants Sage X3 et les autorisations API.",
                    e
            );
        } catch (ResourceAccessException e) {
            throw new IllegalStateException(
                    "Impossible de joindre Sage X3. Le serveur a ferme la connexion ou n'a pas repondu. "
                            + "Verifiez l'URL, le reseau, le port 8124 et l'authentification Sage X3.",
                    e
            );
        }
    }

    private VehicleSyncResult createOrUpdateVehicle(SageVehicleResource resource) {
        String matricule = buildMatricule(resource);
        String normalizedMatricule = MatriculeUtils.normalize(matricule);

        if (normalizedMatricule == null || normalizedMatricule.isBlank()) {
            return VehicleSyncResult.SKIPPED;
        }

        Optional<Vehicle> bySageCode = optionalText(resource.sageCode())
                .flatMap(vehicleRepository::findBySageCode);
        Optional<Vehicle> byMatricule = vehicleRepository.findByNormalizedMatricule(normalizedMatricule);
        Vehicle vehicle = resolveExistingVehicle(bySageCode, byMatricule);
        boolean created = vehicle.getId() == null;
        String rawType = resource.type();
        String mappedType = VehicleTypeMapper.toBusinessLabel(rawType);

        log.debug(
                "Sage vehicle mapping - sageCode={}, matricule={}, rawType={}, mappedType={}",
                resource.sageCode(),
                matricule,
                rawType,
                mappedType
        );

        vehicle.setSageCode(trimToNull(resource.sageCode()));
        vehicle.setMatricule(matricule);
        vehicle.setNormalizedMatricule(normalizedMatricule);
        vehicle.setNumeroChassis(trimToNull(resource.numeroChassis()));
        vehicle.setDateAchat(parseDate(resource.dateAchat()));
        vehicle.setMarque(trimToNull(resource.marque()));
        vehicle.setModele(trimToNull(resource.modele()));
        vehicle.setType(mappedType);
        vehicle.setStatus("CONFORME");
        vehicle.setSource("SAGE_X3");

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.debug(
                "Sage vehicle saved - id={}, sageCode={}, matricule={}, storedType={}",
                savedVehicle.getId(),
                savedVehicle.getSageCode(),
                savedVehicle.getMatricule(),
                savedVehicle.getType()
        );
        infractionService.relinkInfractionsForVehicle(savedVehicle);
        return created ? VehicleSyncResult.CREATED : VehicleSyncResult.UPDATED;
    }

    private Vehicle resolveExistingVehicle(Optional<Vehicle> bySageCode, Optional<Vehicle> byMatricule) {
        if (bySageCode.isPresent() && byMatricule.isPresent()) {
            Long sageId = bySageCode.get().getId();
            Long matriculeId = byMatricule.get().getId();
            return sageId != null && sageId.equals(matriculeId) ? bySageCode.get() : byMatricule.get();
        }

        return bySageCode.or(() -> byMatricule).orElseGet(Vehicle::new);
    }

    private String buildMatricule(SageVehicleResource resource) {
        String numero = trimToNull(resource.numeroEnregistrement());
        String lettre = sageLetter(resource.lettre());
        String prefecture = trimToNull(resource.prefecture());

        if (numero == null || lettre == null || prefecture == null) {
            return null;
        }

        return numero + "-" + lettre + "-" + prefecture;
    }

    private String sageLetter(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        return switch (normalized) {
            case "1" -> "أ";
            case "2" -> "ب";
            case "3" -> "و";
            default -> normalized;
        };
    }

    private LocalDate parseDate(String value) {
        String date = trimToNull(value);
        if (date == null) {
            return null;
        }

        try {
            if (date.matches("\\d{8}")) {
                return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
            }

            if (date.contains("/")) {
                return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            if (date.contains("T")) {
                return LocalDate.parse(date.substring(0, 10));
            }

            return LocalDate.parse(date);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private URI nextUri(URI currentUri, SageVehicleResponse response) {
        String nextUrl = response.links() != null && response.links().next() != null
                ? trimToNull(response.links().next().url())
                : null;

        if (nextUrl == null) {
            return null;
        }

        return currentUri.resolve(nextUrl);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONNECTION, "close");
        String authorizationHeader = authorizationHeader();

        if (authorizationHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        return headers;
    }

    private RestTemplate buildSageRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(15000);
        requestFactory.setReadTimeout(60000);

        return restTemplateBuilder
                .requestFactory(() -> requestFactory)
                .build();
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

    private Optional<String> optionalText(String value) {
        return Optional.ofNullable(trimToNull(value));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private enum VehicleSyncResult {
        CREATED,
        UPDATED,
        SKIPPED
    }
}
