package jbel.annour.vehiclereconciliation.infraction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/infractions")
@RequiredArgsConstructor
public class InfractionController {

    private final InfractionService infractionService;

    @GetMapping
    public ResponseEntity<List<Infraction>> findAll() {
        return ResponseEntity.ok(infractionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Infraction> findById(@PathVariable Long id) {
        return ResponseEntity.ok(infractionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Infraction> create(@RequestBody Infraction infraction) {
        return ResponseEntity.ok(infractionService.create(infraction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Infraction> update(@PathVariable Long id, @RequestBody Infraction infraction) {
        return ResponseEntity.ok(infractionService.update(id, infraction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        infractionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
