package nl.ordina.wot;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import nl.ordina.wot.data.Vehicles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Created by steven on 07-01-17.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wot")
public class WOTController {

    private WOTService tankService;

    @GetMapping("vehicles")
    public Mono<Vehicles> vehicles() {
        return tankService.vehicles()
                .doOnCancel(() -> System.out.println("Cancel"))
                .doOnError(e -> e.printStackTrace());
    }
}
