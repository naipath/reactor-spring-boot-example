package nl.ordina.reactor.playground;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TestingPlayGroundTest {

    public void basicConcept() {
        //Very quickly the basic concept:

        Flux.just("data") // Creating the stream

            .map(String::toUpperCase) // Altering the stream

            .subscribe(System.out::println); // Subscribing to the stream, without this nothing happens


        Mono<String> monoCreate = Mono.create(monoSink -> monoSink.success("Testing"));

        monoCreate.map(String::toUpperCase).subscribe(System.out::println);
    }

    @Test
    public void playGround() {
        //A Very basic test:
        StepVerifier.create(Mono.just("testing this stepverifier"))
            .expectNextCount(1L)
            .verifyComplete();

        StepVerifier.create(Mono.just("This fails on purpose"))
            .verifyError();
    }
}