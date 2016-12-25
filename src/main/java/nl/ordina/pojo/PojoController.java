package nl.ordina.pojo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ordina.webcam.WebcamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Base64;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/pojo")
public class PojoController {

    private ReactivePojoRepository repository;

    private static final Logger log = LoggerFactory.getLogger(PojoController.class);

    @GetMapping("/{id}")
    public Mono<Pojo> get(@PathVariable String id) {
        return repository.findOne(id)
                .doOnError(e -> { log.error(e.getMessage()); } )
                .doOnSuccess(pojo -> log.info("found pojo with id: {}", pojo.getId()))
                .doOnCancel( () -> { log.warn("request cancelled");});

    }

    @PostMapping
    public Mono<Pojo> save(@RequestBody Pojo pojo) {
        return repository.save(pojo);
    }
}