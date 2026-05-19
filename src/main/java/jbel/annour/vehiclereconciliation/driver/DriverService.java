package jbel.annour.vehiclereconciliation.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public Driver findById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chauffeur introuvable"));
    }

    public Driver create(Driver driver) {
        driver.setId(null);
        return driverRepository.save(driver);
    }

    public Driver update(Long id, Driver driver) {
        Driver existingDriver = findById(id);
        existingDriver.setFullName(driver.getFullName());
        existingDriver.setPhone(driver.getPhone());
        existingDriver.setCin(driver.getCin());
        existingDriver.setLicenseNumber(driver.getLicenseNumber());
        existingDriver.setLicenseExpiryDate(driver.getLicenseExpiryDate());
        existingDriver.setStatus(driver.getStatus());
        return driverRepository.save(existingDriver);
    }

    public void delete(Long id) {
        driverRepository.deleteById(id);
    }
}
