package nl.ordina.pojo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
public class PojoController {

    private ReactivePojoRepository repository;

    @GetMapping("/pojo/{id}")
    public Mono<Pojo> get(@PathVariable String id) {
        return repository.findOne(id).doOnNext(pojo -> log.info("found pojo with id: {}", pojo.getId()));
    }

    @PostMapping("/pojo")
    public Mono<Pojo> save(@RequestBody Pojo pojo) {
        return repository.save(pojo);
    }
}