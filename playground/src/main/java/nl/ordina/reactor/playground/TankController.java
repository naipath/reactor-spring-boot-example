package nl.ordina.reactor.playground;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class TankController {

    private TanksRepo tanksRepo;
    private GenericGetClient genericGetClient;

    public TankController(TanksRepo tanksRepo, GenericGetClient genericGetClient) {
        this.tanksRepo = tanksRepo;
        this.genericGetClient = genericGetClient;
    }

    @GetMapping("/tanks/1")
    public String normalMethod() {
        return "{\"name\":\"T-34-85 extended\", \"weight\":7800, \"view_range\":240, \"traverse_left_arc\":180, \"hp\":186, \"traverse_speed\":46, \"tier\":7, \"traverse_right_arc\":180}";
    }

    @GetMapping("/tanks/2")
    public Flux<String> fluxMethod() {
        return Flux.just("{\"name\":\"T-34-85 extended\", \"weight\":7800, \"view_range\":240, \"traverse_left_arc\":180, \"hp\":186, \"traverse_speed\":46, \"tier\":7, \"traverse_right_arc\":180}")
            .delayElements(Duration.ofSeconds(3))
            .map(String::toUpperCase);
    }

    @GetMapping("/tanks/{id}")
    public Mono<Tank> getTank(@PathVariable String id) {
        return tanksRepo.findOne(id);
    }

    @PostMapping("/tanks")
    public Mono<Tank> saveTank(@RequestBody Tank tank) {
        return tanksRepo.save(tank);
    }

    @DeleteMapping("/tanks/{id}")
    public Mono<Void> deleteTank(@PathVariable String id) {
        return tanksRepo.delete(id);
    }

    @GetMapping("/tanks")
    public Flux<Tank> getAll() {
        return tanksRepo.findAll();
    }

    @GetMapping("/webClient/{url}")
    public Flux<String> testing4(@PathVariable String url) {
        return genericGetClient.sendGet(url);
    }
}
