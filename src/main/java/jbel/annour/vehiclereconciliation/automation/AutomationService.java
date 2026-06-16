package jbel.annour.vehiclereconciliation.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jbel.annour.vehiclereconciliation.fuel.FuelConsumption;
import jbel.annour.vehiclereconciliation.fuel.FuelConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutomationService {

    private final AutomationLogRepository automationLogRepository;
    private final ObjectMapper objectMapper;
    private final FuelConsumptionService fuelConsumptionService;

    public List<AutomationLog> findAll() {
        return automationLogRepository.findAll();
    }

    public AutomationLog createLog(String workflowType, Map<String, Object> payload) {
        AutomationLog log = new AutomationLog();
        log.setWorkflowType(workflowType);
        log.setSource("n8n");
        log.setStatus("RECEIVED");
        log.setPayload(toJson(payload));
        log.setMessage("Webhook recu pour " + workflowType);
        log.setCreatedAt(LocalDateTime.now());
        return automationLogRepository.save(log);
    }

    public Map<String, Object> createFuelConsumptionFromWebhook(Map<String, Object> payload) {
        FuelConsumption fuelConsumption = fuelConsumptionService.createFromWebhook(payload);
        String status = fuelConsumption.getStatus();
        boolean vehicleFound = "OK".equals(status);

        AutomationLog log = new AutomationLog();
        log.setWorkflowType("FUEL_CONSUMPTION");
        log.setSource("n8n");
        log.setStatus(status);
        log.setPayload(toJson(payload));
        log.setMessage(vehicleFound
                ? "Consommation gasoil enregistree"
                : "Consommation gasoil enregistree sans vehicule lie");
        log.setCreatedAt(LocalDateTime.now());
        automationLogRepository.save(log);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status);
        response.put("message", log.getMessage());
        response.put("fuelConsumptionId", fuelConsumption.getId());
        response.put("vehicleFound", vehicleFound);
        response.put("vehicleId", fuelConsumption.getVehicleId());
        response.put("automationLogId", log.getId());
        return response;
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return payload.toString();
        }
    }
}
