package jbel.annour.vehiclereconciliation.fuel;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelConsumptionService {

    private final FuelConsumptionRepository fuelConsumptionRepository;

    @Value("${application.upload.fuel-receipts-dir:uploads/fuel-receipts}")
    private String fuelReceiptsDir;

    public List<FuelConsumption> findAll() {
        return fuelConsumptionRepository.findAll();
    }

    public FuelConsumption findById(Long id) {
        return fuelConsumptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consommation gasoil introuvable"));
    }

    public FuelConsumption create(FuelConsumption fuelConsumption) {
        fuelConsumption.setId(null);
        return fuelConsumptionRepository.save(fuelConsumption);
    }

    public FuelConsumption update(Long id, FuelConsumption fuelConsumption) {
        FuelConsumption existingFuelConsumption = findById(id);
        existingFuelConsumption.setVehicleId(fuelConsumption.getVehicleId());
        existingFuelConsumption.setDriverId(fuelConsumption.getDriverId());
        existingFuelConsumption.setMatricule(fuelConsumption.getMatricule());
        existingFuelConsumption.setDriverName(fuelConsumption.getDriverName());
        existingFuelConsumption.setStation(fuelConsumption.getStation());
        existingFuelConsumption.setConsumptionDate(fuelConsumption.getConsumptionDate());
        existingFuelConsumption.setLiters(fuelConsumption.getLiters());
        existingFuelConsumption.setAmount(fuelConsumption.getAmount());
        existingFuelConsumption.setReceiptPhotoPath(fuelConsumption.getReceiptPhotoPath());
        existingFuelConsumption.setNotes(fuelConsumption.getNotes());
        return fuelConsumptionRepository.save(existingFuelConsumption);
    }

    public FuelConsumption attachReceipt(Long id, MultipartFile receipt) throws IOException {
        FuelConsumption fuelConsumption = findById(id);
        Path uploadDir = Path.of(fuelReceiptsDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String originalFilename = receipt.getOriginalFilename() == null ? "receipt" : receipt.getOriginalFilename();
        String filename = UUID.randomUUID() + "-" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = uploadDir.resolve(filename);
        Files.copy(receipt.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        fuelConsumption.setReceiptPhotoPath(target.toString());
        return fuelConsumptionRepository.save(fuelConsumption);
    }

    public void delete(Long id) {
        fuelConsumptionRepository.deleteById(id);
    }
}
