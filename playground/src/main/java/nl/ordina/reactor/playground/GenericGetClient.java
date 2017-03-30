package nl.ordina.reactor.playground;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class GenericGetClient {

    private WebClient webClient = WebClient.create();

    Flux<String> sendGet(String url) {
        return webClient
            .get()
            .uri(url)
            .exchange()
            .flatMap(clientResponse -> clientResponse.bodyToFlux(String.class));
    }
}
