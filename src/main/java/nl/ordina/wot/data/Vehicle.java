package nl.ordina.wot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Created by steven on 07-01-17.
 */
@Getter
@JsonIgnoreProperties
public class Vehicle {
    private String description;
    private List<String> images;
    private String engines;
    private String guns;
    private boolean is_gift;
    private boolean is_premium;
    private boolean is_premium_igr;
    private String name;
    private String nation;
    private Map<String, String> next_tanks;
    private double price_credit;
    private double price_gold;
    private Map<String, String> prices_xp;
    private List<Integer> provisions;
    private List<Integer> radios;
    private String short_name;
    private List<Integer> suspensions;
    private String tag;
    private String tank_id;
    private String tier;
    private List<Integer> turrets;
}
