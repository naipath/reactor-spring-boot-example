package nl.ordina.randombeacon;

import gov.nist.beacon.record._0.Record;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log
@RestController
@RequestMapping("randombeacon")
public class RandomBeaconController {

    private final RandomBeaconService service;

    public RandomBeaconController(RandomBeaconService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<Record> last() {
        return service.last()
                .doOnSuccess(record -> log.info("found beacon with timestamp: " + record.getTimeStamp()))
                .doOnError(e -> { log.severe(e.getMessage()); } )
                .doOnCancel(() -> log.warning("randombeacon cancelled"));
    }
}
