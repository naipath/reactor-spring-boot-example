package nl.ordina.wot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Created by steven on 07-01-17.
 */

@Getter
@Setter
@ToString
@JsonIgnoreProperties
public class Vehicles {
    private String status;
    private Meta meta;
    private Map<String, Vehicle> data;

}
