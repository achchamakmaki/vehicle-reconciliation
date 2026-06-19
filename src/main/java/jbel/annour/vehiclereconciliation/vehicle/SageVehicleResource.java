package jbel.annour.vehiclereconciliation.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SageVehicleResource(
        @JsonProperty("YNUM_0") @JsonAlias("YNUM") String sageCode,
        @JsonProperty("YNUMENRGI_0") @JsonAlias("YNUMENRGI") String numeroEnregistrement,
        @JsonProperty("YLETTRE_0") @JsonAlias("YLETTRE") String lettre,
        @JsonProperty("YPRE_0") @JsonAlias("YPRE") String prefecture,
        @JsonProperty("YNUMCHAS_0") @JsonAlias("YNUMCHAS") String numeroChassis,
        @JsonProperty("YDATACH_0") @JsonAlias("YDATACH") String dateAchat,
        @JsonProperty("YMARQUE_0") @JsonAlias("YMARQUE") String marque,
        @JsonProperty("YMODELE_0") @JsonAlias("YMODELE") String modele,
        @JsonProperty("YTYPE_0") @JsonAlias({"YTYPE", "YTYP", "TYP"}) String type
) {
}
