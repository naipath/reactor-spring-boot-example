package nl.ordina.wot;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import nl.ordina.wot.data.Vehicles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/wot")
public class WOTController {

    private WOTService tankService;

    @GetMapping("vehicles")
    public Mono<Vehicles> vehicles() {
        return tankService.vehicles()
                .doOnSuccess(vehicles -> log.info("Found " + vehicles.getMeta().getCount() + " vehicles."))
                .doOnError(e -> log.severe(e.getMessage()));
    }
}
