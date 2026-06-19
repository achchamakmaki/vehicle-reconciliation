package jbel.annour.vehiclereconciliation.fuel;

import jbel.annour.vehiclereconciliation.util.MatriculeUtils;
import jbel.annour.vehiclereconciliation.vehicle.Vehicle;
import jbel.annour.vehiclereconciliation.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelConsumptionService {

    private final FuelConsumptionRepository fuelConsumptionRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${application.upload.fuel-receipts-dir:uploads/fuel-receipts}")
    private String fuelReceiptsDir;

    public List<FuelConsumption> findAll() {
        return fuelConsumptionRepository.findAllOrderByReceivedAtDesc();
    }

    public FuelConsumption findById(Long id) {
        return fuelConsumptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consommation gasoil introuvable"));
    }

    public FuelConsumption create(FuelConsumption fuelConsumption) {
        fuelConsumption.setId(null);
        if (fuelConsumption.getReceivedAt() == null) {
            fuelConsumption.setReceivedAt(LocalDateTime.now());
        }
        linkVehicle(fuelConsumption);
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
        if (fuelConsumption.getReceivedAt() != null) {
            existingFuelConsumption.setReceivedAt(fuelConsumption.getReceivedAt());
        }
        existingFuelConsumption.setLiters(fuelConsumption.getLiters());
        existingFuelConsumption.setAmount(fuelConsumption.getAmount());
        existingFuelConsumption.setReceiptPhotoPath(fuelConsumption.getReceiptPhotoPath());
        existingFuelConsumption.setReceiptPhotoUrl(fuelConsumption.getReceiptPhotoUrl());
        existingFuelConsumption.setStatus(fuelConsumption.getStatus());
        existingFuelConsumption.setSource(fuelConsumption.getSource());
        existingFuelConsumption.setNotes(fuelConsumption.getNotes());
        existingFuelConsumption.setInvoiceNumber(fuelConsumption.getInvoiceNumber());
        existingFuelConsumption.setProduct(fuelConsumption.getProduct());
        existingFuelConsumption.setPaymentMethod(fuelConsumption.getPaymentMethod());
        existingFuelConsumption.setFuelTime(fuelConsumption.getFuelTime());
        existingFuelConsumption.setUnitPrice(fuelConsumption.getUnitPrice());
        linkVehicle(existingFuelConsumption);
        return fuelConsumptionRepository.save(existingFuelConsumption);
    }

    public FuelConsumption createFromWebhook(Map<String, Object> payload) {
        FuelConsumption fuelConsumption = new FuelConsumption();
        fuelConsumption.setReceivedAt(LocalDateTime.now());
        fuelConsumption.setMatricule(text(payload, "matricule"));
        fuelConsumption.setDriverName(text(payload, "driverName", "chauffeur", "driver"));
        fuelConsumption.setStation(text(payload, "station"));

        fuelConsumption.setConsumptionDate(
                LocalDate.parse(
                        text(payload,"date"),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        fuelConsumption.setLiters(decimal(payload,
                "liters",
                "litres",
                "quantite_litres",
                "quantitelitres"
        ));

        fuelConsumption.setAmount(decimal(payload,
                "amount",
                "montant",
                "montant_total",
                "montanttotal"
        ));

        fuelConsumption.setInvoiceNumber(text(payload,
                "invoiceNumber",
                "numeroFacture",
                "numero_facture",
                "numerofacture"
        ));

        fuelConsumption.setProduct(text(payload,
                "product",
                "produit"
        ));

        fuelConsumption.setPaymentMethod(text(payload,
                "paymentMethod",
                "modePaiement",
                "mode_paiement",
                "modepaiement"
        ));

        fuelConsumption.setFuelTime(text(payload,
                "fuelTime",
                "heure"
        ));

        fuelConsumption.setUnitPrice(decimal(payload,
                "unitPrice",
                "prixUnitaire",
                "prix_unitaire",
                "prixunitaire"
        ));

        fuelConsumption.setSource("N8N");
        fuelConsumption.setReceiptPhotoUrl(text(payload, "receiptPhotoUrl", "receiptUrl", "photoUrl"));
        fuelConsumption.setNotes(text(payload, "notes", "commentaire"));
        linkVehicle(fuelConsumption);
        return fuelConsumptionRepository.save(fuelConsumption);
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
        if (fuelConsumption.getReceivedAt() == null) {
            fuelConsumption.setReceivedAt(LocalDateTime.now());
        }
        return fuelConsumptionRepository.save(fuelConsumption);
    }

    public Resource getReceiptResource(Long id) {
        FuelConsumption fuelConsumption = findById(id);
        if (fuelConsumption.getReceiptPhotoPath() == null || fuelConsumption.getReceiptPhotoPath().isBlank()) {
            throw new IllegalArgumentException("Ticket gasoil introuvable");
        }

        try {
            Path receiptPath = Path.of(fuelConsumption.getReceiptPhotoPath()).toAbsolutePath().normalize();
            if (!Files.exists(receiptPath) || !Files.isRegularFile(receiptPath)) {
                throw new IllegalArgumentException("Ticket gasoil introuvable");
            }

            Resource resource = new UrlResource(receiptPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Ticket gasoil illisible");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Chemin ticket gasoil invalide", e);
        }
    }

    public String getReceiptContentType(Long id) {
        FuelConsumption fuelConsumption = findById(id);
        if (fuelConsumption.getReceiptPhotoPath() == null || fuelConsumption.getReceiptPhotoPath().isBlank()) {
            return "application/octet-stream";
        }

        try {
            String contentType = Files.probeContentType(Path.of(fuelConsumption.getReceiptPhotoPath()));
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public void delete(Long id) {
        fuelConsumptionRepository.deleteById(id);
    }

    private void linkVehicle(FuelConsumption fuelConsumption) {
        String normalizedMatricule = MatriculeUtils.normalize(fuelConsumption.getMatricule());
        if (normalizedMatricule == null || normalizedMatricule.isBlank()) {
            fuelConsumption.setVehicleId(null);
            fuelConsumption.setStatus("VEHICULE_INTROUVABLE");
            return;
        }

        Optional<Vehicle> vehicle = vehicleRepository.findByNormalizedMatricule(normalizedMatricule);
        if (vehicle.isPresent()) {
            fuelConsumption.setVehicleId(vehicle.get().getId());
            fuelConsumption.setStatus("OK");
        } else {
            fuelConsumption.setVehicleId(null);
            fuelConsumption.setStatus("VEHICULE_INTROUVABLE");
        }
    }

    private String text(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString().trim();
            }
        }
        return null;
    }

    private BigDecimal decimal(Map<String, Object> payload, String... keys) {
        String value = text(payload, keys);
        if (value == null) {
            return null;
        }

        String normalized = value.replace(" ", "").replace(",", ".");
        return new BigDecimal(normalized);
    }

    private LocalDate date(Map<String, Object> payload, String... keys) {
        String value = text(payload, keys);

        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }

        if (value.contains("/")) {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        return LocalDate.parse(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public void saveReceiptImage(Long id, MultipartFile file) throws IOException {
        FuelConsumption fuel = fuelConsumptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consommation introuvable"));

        String uploadDir = "uploads/fuel-receipts/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = id + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        fuel.setReceiptPhotoPath(filePath.toString());
        fuel.setReceiptPhotoUrl("/api/fuel-consumptions/" + id + "/receipt");
        if (fuel.getReceivedAt() == null) {
            fuel.setReceivedAt(LocalDateTime.now());
        }

        fuelConsumptionRepository.save(fuel);
    }

    public Resource getReceiptImage(Long id) throws IOException {
        FuelConsumption fuel = fuelConsumptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consommation introuvable"));

        if (fuel.getReceiptPhotoPath() == null) {
            throw new RuntimeException("Photo ticket introuvable");
        }

        Path path = Paths.get(fuel.getReceiptPhotoPath());
        return new UrlResource(path.toUri());
    }


}
