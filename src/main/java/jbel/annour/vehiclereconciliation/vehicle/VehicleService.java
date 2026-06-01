package jbel.annour.vehiclereconciliation.vehicle;

import jbel.annour.vehiclereconciliation.entity.ComparisonResult;
import jbel.annour.vehiclereconciliation.entity.VehicleNarsa;
import jbel.annour.vehiclereconciliation.entity.VehicleSage;
import jbel.annour.vehiclereconciliation.repository.ComparisonResultRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleNarsaRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ComparisonResultRepository comparisonResultRepository;
    private final VehicleNarsaRepository vehicleNarsaRepository;
    private final VehicleSageRepository vehicleSageRepository;

    @Transactional
    public List<Vehicle> findAll() {
        syncSageDetailsFromSage();
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicule introuvable"));
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        vehicle.setId(null);
        vehicle.setNormalizedMatricule(normalizeMatricule(vehicle.getMatricule()));
        return createOrUpdateManualVehicle(vehicle);
    }

    @Transactional
    public Vehicle update(Long id, Vehicle vehicle) {
        Vehicle existingVehicle = findById(id);
        existingVehicle.setMatricule(vehicle.getMatricule());
        existingVehicle.setNormalizedMatricule(normalizeMatricule(vehicle.getMatricule()));
        existingVehicle.setSageCode(vehicle.getSageCode());
        existingVehicle.setMarque(vehicle.getMarque());
        existingVehicle.setModele(vehicle.getModele());
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setStatus(vehicle.getStatus());
        existingVehicle.setSource(firstNonBlank(vehicle.getSource(), existingVehicle.getSource(), "MANUAL"));
        return vehicleRepository.save(existingVehicle);
    }

    @Transactional
    public void delete(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
        }
    }

    @Transactional
    public void syncVehiclesFromReconciliation() {
        comparisonResultRepository.findAll()
                .forEach(result -> {
                    String normalizedMatricule = normalizeMatricule(result.getMatricule());
                    if (normalizedMatricule.isBlank()) {
                        return;
                    }

                    VehicleNarsa narsa = vehicleNarsaRepository.findByNormalizedMatricule(normalizedMatricule)
                            .stream()
                            .findFirst()
                            .orElse(null);
                    VehicleSage sage = vehicleSageRepository.findByNormalizedMatricule(normalizedMatricule)
                            .stream()
                            .findFirst()
                            .orElse(null);

                    Vehicle vehicle = buildVehicleFromComparison(result, narsa, sage, normalizedMatricule);
                    createOrUpdateVehicleFromReconciliation(vehicle);
                });

        syncSageDetailsFromSage();
    }

    @Transactional
    public void syncSageDetailsFromSage() {
        vehicleSageRepository.findAll().forEach(sage -> {
            if (sage.getNormalizedMatricule() == null || sage.getNormalizedMatricule().isBlank()) {
                return;
            }

            vehicleRepository.findByNormalizedMatricule(sage.getNormalizedMatricule())
                    .ifPresent(vehicle -> {
                        vehicle.setSageCode(sage.getSageCode());
                        vehicle.setMarque(sage.getMarque());
                        vehicle.setModele(sage.getModele());

                        if (vehicle.getMatricule() == null || vehicle.getMatricule().isBlank()) {
                            vehicle.setMatricule(sage.getNoImmatriculation());
                        }

                        vehicleRepository.save(vehicle);
                    });
        });
    }

    private Vehicle createOrUpdateVehicleFromReconciliation(Vehicle vehicle) {
        String normalizedMatricule = vehicle.getNormalizedMatricule();
        if (normalizedMatricule.isBlank()) {
            throw new IllegalArgumentException("Matricule obligatoire");
        }

        Vehicle existingVehicle = vehicleRepository.findByNormalizedMatricule(normalizedMatricule)
                .orElseGet(Vehicle::new);

        existingVehicle.setMatricule(vehicle.getMatricule());
        existingVehicle.setNormalizedMatricule(normalizedMatricule);
        existingVehicle.setSageCode(vehicle.getSageCode());
        existingVehicle.setMarque(firstNonBlank(vehicle.getMarque(), "N/A"));
        existingVehicle.setModele(firstNonBlank(vehicle.getModele(), "N/A"));
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setStatus(vehicle.getStatus());
        existingVehicle.setSource(vehicle.getSource());

        return vehicleRepository.save(existingVehicle);
    }

    private Vehicle createOrUpdateManualVehicle(Vehicle vehicle) {
        String normalizedMatricule = vehicle.getNormalizedMatricule();
        if (normalizedMatricule == null || normalizedMatricule.isBlank()) {
            throw new IllegalArgumentException("Matricule obligatoire");
        }

        Vehicle existingVehicle = vehicleRepository.findByNormalizedMatricule(normalizedMatricule)
                .orElseGet(Vehicle::new);

        existingVehicle.setMatricule(vehicle.getMatricule());
        existingVehicle.setNormalizedMatricule(normalizedMatricule);
        existingVehicle.setSageCode(vehicle.getSageCode());
        existingVehicle.setMarque(vehicle.getMarque());
        existingVehicle.setModele(vehicle.getModele());
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setStatus(firstNonBlank(vehicle.getStatus(), "CONFORME"));
        existingVehicle.setSource(firstNonBlank(vehicle.getSource(), "MANUAL"));

        return vehicleRepository.save(existingVehicle);
    }

    private Vehicle buildVehicleFromComparison(
            ComparisonResult result,
            VehicleNarsa narsa,
            VehicleSage sage,
            String normalizedMatricule
    ) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMatricule(firstNonBlank(result.getMatricule(), narsaMatricule(narsa), sageMatricule(sage)));
        vehicle.setNormalizedMatricule(normalizedMatricule);
        vehicle.setSageCode(sage != null ? sage.getSageCode() : null);
        vehicle.setMarque(sage != null ? firstNonBlank(sage.getMarque(), "N/A") : "N/A");
        vehicle.setModele(sage != null ? firstNonBlank(sage.getModele(), "N/A") : "N/A");
        vehicle.setStatus(toVehicleStatus(result.getStatus()));
        vehicle.setSource(resolveSource(narsa, sage));
        return vehicle;
    }

    private String toVehicleStatus(String comparisonStatus) {
        if ("MATCH".equalsIgnoreCase(comparisonStatus)) {
            return "CONFORME";
        }

        return comparisonStatus;
    }

    private String normalizeMatricule(String matricule) {
        String normalizedMatricule = MatriculeUtils.normalize(matricule);
        return normalizedMatricule != null ? normalizedMatricule : "";
    }

    private String narsaMatricule(VehicleNarsa narsa) {
        return narsa != null ? narsa.getNoImmatriculation() : null;
    }

    private String sageMatricule(VehicleSage sage) {
        return sage != null ? sage.getNoImmatriculation() : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String resolveSource(VehicleNarsa narsa, VehicleSage sage) {
        if (narsa != null && sage != null) {
            return "NARSA_SAGE";
        }

        if (narsa != null) {
            return "NARSA";
        }

        return "SAGE";
    }
}
