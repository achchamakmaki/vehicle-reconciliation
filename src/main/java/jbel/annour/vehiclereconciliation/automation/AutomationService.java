package jbel.annour.vehiclereconciliation.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutomationService {

    private final AutomationLogRepository automationLogRepository;
    private final ObjectMapper objectMapper;

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

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return payload.toString();
        }
    }
}
