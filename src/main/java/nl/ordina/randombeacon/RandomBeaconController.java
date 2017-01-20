package nl.ordina.randombeacon;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("randombeacon")
public class RandomBeaconController {

    private final RandomBeaconService service;

    public RandomBeaconController(RandomBeaconService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<String> last() {
        return service.last();
    }
}
