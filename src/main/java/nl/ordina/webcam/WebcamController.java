package nl.ordina.webcam;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Created by steven on 25-12-16.
 */
@RestController
@AllArgsConstructor
@RequestMapping(path = "/webcam")
public class WebcamController {

    private WebcamService webcamService;

    @GetMapping(produces =  MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> get() {
        return webcamService.getImage();
    }
}
