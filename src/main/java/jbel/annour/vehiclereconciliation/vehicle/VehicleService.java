package jbel.annour.vehiclereconciliation.vehicle;

import jbel.annour.vehiclereconciliation.entity.ComparisonResult;
import jbel.annour.vehiclereconciliation.entity.VehicleNarsa;
import jbel.annour.vehiclereconciliation.entity.VehicleSage;
import jbel.annour.vehiclereconciliation.repository.ComparisonResultRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleNarsaRepository;
import jbel.annour.vehiclereconciliation.repository.VehicleSageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ComparisonResultRepository comparisonResultRepository;
    private final VehicleNarsaRepository vehicleNarsaRepository;
    private final VehicleSageRepository vehicleSageRepository;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicule introuvable"));
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        vehicle.setId(null);
        return createOrUpdateVehicle(vehicle);
    }

    @Transactional
    public Vehicle update(Long id, Vehicle vehicle) {
        Vehicle existingVehicle = findById(id);
        existingVehicle.setMatricule(vehicle.getMatricule());
        existingVehicle.setNormalizedMatricule(normalizeMatricule(vehicle.getMatricule()));
        existingVehicle.setMarque(vehicle.getMarque());
        existingVehicle.setModele(vehicle.getModele());
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setStatus(vehicle.getStatus());
        existingVehicle.setSageReference(vehicle.getSageReference());
        existingVehicle.setNarsaReference(vehicle.getNarsaReference());
        return vehicleRepository.save(existingVehicle);
    }

    @Transactional
    public void delete(Long id) {
        vehicleRepository.deleteById(id);
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
                    createOrUpdateVehicle(vehicle);
                });
    }

    @Transactional
    public Vehicle createOrUpdateVehicle(Vehicle vehicle) {
        String normalizedMatricule = normalizeMatricule(vehicle.getMatricule());
        if (normalizedMatricule.isBlank()) {
            throw new IllegalArgumentException("Matricule obligatoire");
        }

        Vehicle existingVehicle = vehicleRepository.findByNormalizedMatricule(normalizedMatricule)
                .orElseGet(Vehicle::new);

        existingVehicle.setMatricule(firstNonBlank(vehicle.getMatricule(), existingVehicle.getMatricule()));
        existingVehicle.setNormalizedMatricule(normalizedMatricule);
        existingVehicle.setMarque(firstNonBlank(vehicle.getMarque(), existingVehicle.getMarque()));
        existingVehicle.setModele(firstNonBlank(vehicle.getModele(), existingVehicle.getModele()));
        existingVehicle.setType(firstNonBlank(vehicle.getType(), existingVehicle.getType()));
        existingVehicle.setStatus(firstNonBlank(vehicle.getStatus(), existingVehicle.getStatus()));
        existingVehicle.setSageReference(firstNonBlank(vehicle.getSageReference(), existingVehicle.getSageReference()));
        existingVehicle.setNarsaReference(firstNonBlank(vehicle.getNarsaReference(), existingVehicle.getNarsaReference()));

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
        vehicle.setMarque(sage != null ? sage.getMarque() : null);
        vehicle.setModele(sage != null ? sage.getModele() : null);
        vehicle.setStatus(toVehicleStatus(result.getStatus()));
        vehicle.setSageReference(sage != null ? firstNonBlank(sage.getSageCode(), sage.getNoImmatriculation()) : null);
        vehicle.setNarsaReference(narsa != null ? narsa.getNoImmatriculation() : null);
        return vehicle;
    }

    private String toVehicleStatus(String comparisonStatus) {
        if ("MATCH".equalsIgnoreCase(comparisonStatus)) {
            return "CONFORME";
        }

        return comparisonStatus;
    }

    private String normalizeMatricule(String matricule) {
        if (matricule == null) {
            return "";
        }

        return matricule.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String narsaMatricule(VehicleNarsa narsa) {
        return narsa != null ? narsa.getNoImmatriculation() : null;
    }

    private String sageMatricule(VehicleSage sage) {
        return sage != null ? sage.getNoImmatriculation() : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (Objects.nonNull(value) && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}
