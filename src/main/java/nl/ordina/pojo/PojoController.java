package nl.ordina.pojo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/pojo")
public class PojoController {

    private ReactivePojoRepository repository;

    @GetMapping("/{id}")
    public Mono<Pojo> get(@PathVariable String id) {
        return repository.findOne(id)
            .doOnError(e -> log.error(e.getMessage()))
            .doOnSuccess(pojo -> log.info("found pojo with id: {}", pojo.getId()))
            .doOnCancel(() -> log.warn("request cancelled"));

    }

    @PostMapping
    public Mono<Pojo> save(@RequestBody Pojo pojo) {
        return repository.save(pojo);
    }
}