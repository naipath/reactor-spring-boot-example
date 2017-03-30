package nl.ordina.web.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Created by steven on 07-01-17.
 */
public class AsyncGet<T> {

    private final Class<T> cls;
    private final AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    public AsyncGet(Class<T> cls) {
        this.cls = cls;
    }

    public Mono<T> get(URI uri) {
        CompletableFuture<ResponseEntity<T>> future = new CompletableFuture<>();
        asyncRestTemplate.getForEntity(uri, cls).addCallback(
                entity -> future.complete(entity),
                ex -> future.completeExceptionally(ex)
        );
        return Mono.fromFuture(future).map(HttpEntity::getBody);
    }
}
