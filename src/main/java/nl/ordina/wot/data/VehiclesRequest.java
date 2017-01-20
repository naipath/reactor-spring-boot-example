package nl.ordina.wot.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VehiclesRequest {
    public static final VehiclesRequest ALL = new VehiclesRequest(
            "en",
            "",
            "",
            ""
    );

    private String language = "en";
    private String type;
    private String tank_d;
    private String tier;
}
