package nl.ordina.pojo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface ReactivePojoRepository extends ReactiveCrudRepository<Pojo, String> {}