package nl.ordina.whois;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static net.javacrumbs.futureconverter.springjava.FutureConverter.toCompletableFuture;

@Service
class WhoisService {

    private AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    Mono<List<Contact>> get(String domain) {
        URI uri = createBasicUri().queryParam("domain", domain).build().toUri();

        return Mono.fromFuture(toCompletableFuture(asyncRestTemplate.getForEntity(uri, Whois.class)))
            .map(HttpEntity::getBody)
            .map(Whois::getContacts);
    }

    private UriComponentsBuilder createBasicUri() {
        return UriComponentsBuilder
            .fromHttpUrl("http://api.whoapi.com")
            .queryParam("r", "whois")
            .queryParam("apikey", "cbd08c8c4b3835d88ba1962e4aa1914c");
    }
}
