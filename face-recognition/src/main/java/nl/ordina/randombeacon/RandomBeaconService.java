package nl.ordina.randombeacon;

import gov.nist.beacon.record._0.Record;
import lombok.AllArgsConstructor;
import nl.ordina.web.client.AsyncGet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class RandomBeaconService {

    private AsyncGet<Record> last = new AsyncGet<>(Record.class);

    public Mono<Record> last() {
        return last.get(URI.create("https://beacon.nist.gov/rest/record/last"));
    }
}
