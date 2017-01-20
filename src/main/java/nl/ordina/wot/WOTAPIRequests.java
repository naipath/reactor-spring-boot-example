package nl.ordina.wot;

import nl.ordina.web.client.AsyncPost;
import nl.ordina.wot.data.Vehicles;
import nl.ordina.wot.data.VehiclesRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by steven on 07-01-17.
 */
@Component
public class WOTAPIRequests {

    @Bean
    AsyncPost<VehiclesRequest, Vehicles> vehicles() {
        return new AsyncPost<VehiclesRequest, Vehicles>(uriBuilder("/wot/encyclopedia/vehicles/"), Vehicles.class, new WOTMessageConverter());
    }



    private UriComponentsBuilder uriBuilder(String path) {
        return UriComponentsBuilder
                .fromHttpUrl("https://api.worldoftanks.ru")
                .queryParam("application_id","demo")
                .path(path);
    }

}
