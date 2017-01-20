package nl.ordina.wot;

import com.fasterxml.jackson.annotation.JsonValue;
import nl.ordina.web.client.AsyncPost;
import nl.ordina.wot.data.Vehicle;
import nl.ordina.wot.data.Vehicles;
import nl.ordina.wot.data.VehiclesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by steven on 06-01-17.
 */
@Service
public class WOTService {

    @Autowired
    private AsyncPost<VehiclesRequest, Vehicles> vehicles;

    public Mono<Vehicles> vehicles() {
        return vehicles.post(VehiclesRequest.ALL);
    }

    public Mono<Vehicles> vehicles(VehiclesRequest request) {
        return vehicles.post(request);
    }

}
