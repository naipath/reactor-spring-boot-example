package nl.ordina.pojo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/pojo")
public class PojoController {

    private ReactivePojoRepository repository;
    private WebcamService webcamService;

    @GetMapping("/{id}")
    public Mono<Tuple2<Pojo, String>> get(@PathVariable String id) {
        return repository.findOne(id)
            .doOnNext(pojo -> log.info("found pojo with id: {}", pojo.getId()))
            .and(webcamService.getImage());
    }

    @PostMapping
    public Mono<Pojo> save(@RequestBody Pojo pojo) {
        return repository.save(pojo);
    }
}