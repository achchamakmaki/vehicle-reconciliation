package jbel.annour.vehiclereconciliation.fuel;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/fuel-consumptions")
@RequiredArgsConstructor
public class FuelConsumptionController {

    private final FuelConsumptionService fuelConsumptionService;

    @GetMapping
    public ResponseEntity<List<FuelConsumption>> findAll() {
        return ResponseEntity.ok(fuelConsumptionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuelConsumption> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fuelConsumptionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FuelConsumption> create(@RequestBody FuelConsumption fuelConsumption) {
        return ResponseEntity.ok(fuelConsumptionService.create(fuelConsumption));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuelConsumption> update(@PathVariable Long id, @RequestBody FuelConsumption fuelConsumption) {
        return ResponseEntity.ok(fuelConsumptionService.update(id, fuelConsumption));
    }

    /*@PostMapping("/{id}/receipt")
    public ResponseEntity<FuelConsumption> attachReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile receipt
    ) throws IOException {
        return ResponseEntity.ok(fuelConsumptionService.attachReceipt(id, receipt));
    }*/

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FuelConsumption> attachReceipt(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile receipt
    ) throws IOException {

        System.out.println("UPLOAD RECEIPT ID = " + id);
        System.out.println("FILE NAME = " + receipt.getOriginalFilename());
        System.out.println("FILE SIZE = " + receipt.getSize());
        System.out.println("CONTENT TYPE = " + receipt.getContentType());

        FuelConsumption updated = fuelConsumptionService.attachReceipt(id, receipt);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Resource> getReceipt(@PathVariable Long id) {
        Resource receipt;
        String contentType;

        try {
            receipt = fuelConsumptionService.getReceiptResource(id);
            contentType = fuelConsumptionService.getReceiptContentType(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(contentType))
                .body(receipt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fuelConsumptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /*@PostMapping("/webhook/n8n")
    public ResponseEntity<FuelConsumption> receiveFromN8n(
            @RequestBody Map<String, Object> payload) {

        return ResponseEntity.ok(
                fuelConsumptionService.createFromWebhook(payload)
        );
    }

    @PostMapping("/webhook/n8n")
    public ResponseEntity<?> test(@RequestBody Map<String, Object> body) {
        System.out.println("BODY RECU = " + body);
        return ResponseEntity.ok(body);
    }*/

    @PostMapping("/webhook/n8n")
    public ResponseEntity<FuelConsumption> receiveFromN8n(@RequestBody Map<String, Object> body) {
        System.out.println("BODY RECU = " + body);
        FuelConsumption saved = fuelConsumptionService.createFromWebhook(body);
        return ResponseEntity.ok(saved);
    }



}
