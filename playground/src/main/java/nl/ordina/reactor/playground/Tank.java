package nl.ordina.reactor.playground;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Getter
class Tank {
    private @Id String name;
    private String health;
    private String color;
}
