package nl.ordina.web.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Created by steven on 07-01-17.
 */
public class AsyncPost<T,R> {

    private final AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
    private final UriComponentsBuilder builder;
    private final Class<R> cls;

    public AsyncPost(UriComponentsBuilder builder, Class<R> cls) {
        this.builder = builder;
        this.cls = cls;
    }

    public AsyncPost(UriComponentsBuilder builder, Class<R> cls, HttpMessageConverter<? super R> messageConverter) {
        this(builder, cls);
        asyncRestTemplate.getMessageConverters().clear();
        asyncRestTemplate.getMessageConverters().add(messageConverter);
    }


    public Mono<R> post(T request) {
        CompletableFuture<ResponseEntity<R>> future = new CompletableFuture<>();
        HttpEntity<T> httpRequest = new HttpEntity<>(request);

        URI uri = builder.build().toUri();
        asyncRestTemplate.postForEntity(uri, httpRequest, cls).addCallback(
                entity -> future.complete(entity),
                ex -> future.completeExceptionally(ex)
        );
        return Mono.fromFuture(future).map(HttpEntity::getBody);

    }
}
