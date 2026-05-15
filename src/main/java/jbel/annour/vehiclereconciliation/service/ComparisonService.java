package jbel.annour.vehiclereconciliation.service;

import jbel.annour.vehiclereconciliation.entity.*;
import jbel.annour.vehiclereconciliation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final VehicleNarsaRepository narsaRepository;
    private final VehicleSageRepository sageRepository;
    private final ComparisonResultRepository resultRepository;

    @Transactional
    public void compareVehicles() {

        resultRepository.deleteAll();

        List<VehicleNarsa> narsaVehicles = narsaRepository.findAll();
        List<VehicleSage> sageVehicles = sageRepository.findAll();

        Set<String> sageMatricules = sageVehicles.stream()
                .map(VehicleSage::getNormalizedMatricule)
                .collect(Collectors.toSet());

        Set<String> narsaMatricules = narsaVehicles.stream()
                .map(VehicleNarsa::getNormalizedMatricule)
                .collect(Collectors.toSet());

        for (VehicleNarsa narsa : narsaVehicles) {
            ComparisonResult result = new ComparisonResult();
            result.setMatricule(narsa.getNoImmatriculation());

            if (sageMatricules.contains(narsa.getNormalizedMatricule())) {
                result.setStatus("MATCH");
                result.setDetails("Vehicle exists in both systems");
            } else {
                result.setStatus("ABSENT_IN_SAGE");
                result.setDetails("Vehicle exists in NARSA only");
            }

            resultRepository.save(result);
        }

        for (VehicleSage sage : sageVehicles) {
            if (!narsaMatricules.contains(sage.getNormalizedMatricule())) {
                ComparisonResult result = new ComparisonResult();
                result.setMatricule(sage.getNoImmatriculation());
                result.setStatus("ABSENT_IN_NARSA");
                result.setDetails("Vehicle exists in Sage only");
                resultRepository.save(result);
            }
        }
    }
}