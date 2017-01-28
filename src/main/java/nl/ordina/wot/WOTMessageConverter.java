package nl.ordina.wot;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ordina.wot.data.Meta;
import nl.ordina.wot.data.Vehicle;
import nl.ordina.wot.data.Vehicles;
import nl.ordina.wot.data.VehiclesRequest;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WOTMessageConverter implements HttpMessageConverter<Object> {

    private JsonFactory factory = new JsonFactory();

    @Override
    public boolean canRead(Class<?> aClass, MediaType mediaType) {
        return aClass.equals(Vehicles.class) || aClass.equals(VehiclesRequest.class);
    }

    @Override
    public boolean canWrite(Class<?> aClass, MediaType mediaType) {
        return aClass.equals(VehiclesRequest.class);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.APPLICATION_JSON);
    }

    @Override
    public Object read(Class<? extends Object> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {

        if(Vehicles.class.equals(aClass)) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(httpInputMessage.getBody());

            Vehicles vehicles = new Vehicles();
            vehicles.setData(new HashMap<>());
            vehicles.setStatus(node.get("status").asText());
            vehicles.setMeta(objectMapper.treeToValue(node.get("meta"), Meta.class));

            JsonNode data = node.get("data");
            Iterator<String> fieldNames = data.fieldNames();
            while(fieldNames.hasNext()) {
                String vehicleId = fieldNames.next();
                JsonNode vehicleNode = data.get(vehicleId);
                Vehicle vehicle = objectMapper.treeToValue(vehicleNode, Vehicle.class);
                vehicles.getData().put(vehicleId, vehicle);
            }

            return vehicles;

        }

        if(VehiclesRequest.class.equals(aClass)) {
            ObjectMapper objectMapper = new ObjectMapper();
            VehiclesRequest vehiclesRequest = objectMapper.readValue(httpInputMessage.getBody(), VehiclesRequest.class);
            return vehiclesRequest;
        }

        return null;
    }

    @Override
    public void write(Object object, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        if(VehiclesRequest.class.equals(object.getClass())) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(httpOutputMessage.getBody(), object);
        }
    }
}
