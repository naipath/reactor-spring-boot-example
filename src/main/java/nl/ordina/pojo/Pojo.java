package nl.ordina.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
class Pojo {
    private @Id String id;
    private String field1;
    private String field2;
}
