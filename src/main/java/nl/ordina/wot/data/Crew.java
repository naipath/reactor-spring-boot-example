package nl.ordina.wot.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Crew {
    Map<String, String> roles;
    String member_id;
}
