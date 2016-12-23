package nl.ordina.pojo;

import com.github.sarxos.webcam.Webcam;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Service
public class WebcamService {

    private Webcam webcam;

    public WebcamService() {
        this.webcam = Webcam.getDefault();
        webcam.open();
    }

    public Mono<String> getImage() {
        return Mono.just(new String(Base64.getEncoder().encode(webcam.getImageBytes()).array()));
    }
}
