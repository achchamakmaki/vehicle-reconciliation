package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SageVehicleNextLink(
        @JsonProperty("$url") String url
) {
}
