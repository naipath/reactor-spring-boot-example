package nl.ordina.reactor.playground;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TanksRepo extends ReactiveCrudRepository<Tank, String> {
}
