package nl.ordina.pojo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactivePojoRepository extends ReactiveCrudRepository<Pojo, String> {}