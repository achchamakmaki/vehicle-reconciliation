package jbel.annour.vehiclereconciliation.infraction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InfractionService {

    private final InfractionRepository infractionRepository;

    public List<Infraction> findAll() {
        return infractionRepository.findAll();
    }

    public Infraction findById(Long id) {
        return infractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Infraction introuvable"));
    }

    public Infraction create(Infraction infraction) {
        infraction.setId(null);
        return infractionRepository.save(infraction);
    }

    public Infraction update(Long id, Infraction infraction) {
        Infraction existingInfraction = findById(id);
        existingInfraction.setVehicleId(infraction.getVehicleId());
        existingInfraction.setDriverId(infraction.getDriverId());
        existingInfraction.setMatricule(infraction.getMatricule());
        existingInfraction.setDriverName(infraction.getDriverName());
        existingInfraction.setInfractionDate(infraction.getInfractionDate());
        existingInfraction.setLocation(infraction.getLocation());
        existingInfraction.setType(infraction.getType());
        existingInfraction.setAmount(infraction.getAmount());
        existingInfraction.setPaymentStatus(infraction.getPaymentStatus());
        existingInfraction.setReference(infraction.getReference());
        existingInfraction.setNotes(infraction.getNotes());
        return infractionRepository.save(existingInfraction);
    }

    public void delete(Long id) {
        infractionRepository.deleteById(id);
    }
}
