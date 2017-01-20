package nl.ordina.whois;

import nl.ordina.web.client.AsyncGet;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSource;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
class WhoisService {

    private AsyncGet<Whois> whoisRequest = new AsyncGet<>(Whois.class);

    Mono<List<Contact>> get(String domain) {
        URI uri = createBasicUri().queryParam("domain", domain).build().toUri();
        return whoisRequest.get(uri).map(Whois::getContacts);
    }

    private UriComponentsBuilder createBasicUri() {
        return UriComponentsBuilder
            .fromHttpUrl("http://api.whoapi.com")
            .queryParam("r", "whois")
            .queryParam("apikey", "cbd08c8c4b3835d88ba1962e4aa1914c");
    }
}
