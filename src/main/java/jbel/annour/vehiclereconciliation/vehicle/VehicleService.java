package jbel.annour.vehiclereconciliation.vehicle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicule introuvable"));
    }

    public Vehicle create(Vehicle vehicle) {
        vehicle.setId(null);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle vehicle) {
        Vehicle existingVehicle = findById(id);
        existingVehicle.setMatricule(vehicle.getMatricule());
        existingVehicle.setBrand(vehicle.getBrand());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setStatus(vehicle.getStatus());
        existingVehicle.setSageReference(vehicle.getSageReference());
        existingVehicle.setNarsaReference(vehicle.getNarsaReference());
        existingVehicle.setCirculationDate(vehicle.getCirculationDate());
        return vehicleRepository.save(existingVehicle);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }
}
