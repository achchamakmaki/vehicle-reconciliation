package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SageVehicleLinks(
        @JsonProperty("$next") SageVehicleNextLink next
) {
}
