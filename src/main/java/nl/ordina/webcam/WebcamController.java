package nl.ordina.webcam;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Created by steven on 25-12-16.
 */
@Log
@RestController
@AllArgsConstructor
@RequestMapping(path = "/webcam")
public class WebcamController {

    private WebcamService webcamService;

    @GetMapping(produces =  MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> get() {
        return webcamService.getImage();
    }

    @GetMapping(path="/subscribe", produces =  MediaType.IMAGE_JPEG_VALUE)
    public void subscribe() {
        webcamService.subscribe();
    }

    @GetMapping(path="/unsubscribe", produces =  MediaType.IMAGE_JPEG_VALUE)
    public void unsubscribe() {
         webcamService.unsubscribe();
    }


}
