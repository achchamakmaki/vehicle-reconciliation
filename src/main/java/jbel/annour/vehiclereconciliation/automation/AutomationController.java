package jbel.annour.vehiclereconciliation.automation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RestController
@RequestMapping("/api/automations")
@RequiredArgsConstructor
public class AutomationController {

    private final AutomationService automationService;

    @GetMapping("/logs")
    public ResponseEntity<List<AutomationLog>> findLogs() {
        return ResponseEntity.ok(automationService.findAll());
    }

    @PostMapping("/webhooks/fuel-consumption")
    public ResponseEntity<Map<String, Object>> fuelConsumptionWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(automationService.createFuelConsumptionFromWebhook(payload));
    }

    @PostMapping("/webhooks/infraction")
    public ResponseEntity<AutomationLog> infractionWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(automationService.createLog("INFRACTION", payload));
    }

    @PostMapping("/webhooks/weekly-report")
    public ResponseEntity<AutomationLog> weeklyReportWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(automationService.createLog("WEEKLY_REPORT", payload));
    }
}
