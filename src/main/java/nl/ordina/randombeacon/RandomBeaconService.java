package nl.ordina.randombeacon;

import lombok.AllArgsConstructor;
import nl.ordina.web.client.AsyncGet;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class RandomBeaconService {

    private AsyncGet<String> last = new AsyncGet<>(String.class);

    public Mono<String> last() {
        return last.get(URI.create("https://beacon.nist.gov/rest/record/last"));
    }
}
