package nl.ordina.wot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by steven on 07-01-17.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {
    private String description;
    private Map<String, String> images;
    private List<Integer> engines;
    private List<Integer> guns;
    @JsonProperty("is_gift")
    private boolean is_gift;
    @JsonProperty("is_premium")
    private boolean is_premium;
    @JsonProperty("is_premium_igr")
    private boolean is_premium_igr;
    private String name;
    private String nation;
    private Map<String, String> next_tanks;
    private double price_credit;
    private double price_gold;
    private List<Integer> radios;
    private String short_name;
    private List<Integer> suspensions;
    private String tag;
    private String tank_id;
    private String tier;
    private List<Integer> turrets;
    private List<Crew> crew;
    @JsonProperty("type")
    private String vehicleType;
    private List<Integer> provisions;
    //    private Map<String, String> prices_xp;
}
