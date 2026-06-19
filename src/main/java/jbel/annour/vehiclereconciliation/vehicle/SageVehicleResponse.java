package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SageVehicleResponse(
        @JsonProperty("$resources") List<SageVehicleResource> resources,
        @JsonProperty("$links") SageVehicleLinks links
) {
}
