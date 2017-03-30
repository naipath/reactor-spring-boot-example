package nl.ordina.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Getter
class Pojo {
    private @Id String id;
    private String field1;
    private String field2;
}
