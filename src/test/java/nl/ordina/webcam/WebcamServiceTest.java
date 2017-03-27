package nl.ordina.webcam;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

public class WebcamServiceTest {

    @Test
    public void test() {
        StepVerifier.create(Flux.just("abc","efg")).expectNext("abc");
    }

}