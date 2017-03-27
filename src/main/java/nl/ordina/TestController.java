package nl.ordina;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TestController {

    @GetMapping("/test/{id}")
    public Flux<Integer> testing(@PathVariable String id) {
        return Flux.just(id).map(String::length);
    }

    @GetMapping("/test/http2")
    public String http2() {
        return "http2";
    }
}