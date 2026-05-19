package jbel.annour.vehiclereconciliation.automation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_automation_logs")
@Data
public class AutomationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workflowType;
    private String source;
    private String status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    private String message;
    private LocalDateTime createdAt;
}
