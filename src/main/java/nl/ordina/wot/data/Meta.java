package nl.ordina.wot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * Created by steven on 07-01-17.
 */
@Getter
@JsonIgnoreProperties
public class Meta {
    private int count;
}
