package nl.ordina.webcam;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.opencv.core.Mat;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by steven on 25-12-16.
 */
@Log
@RestController
@AllArgsConstructor
@RequestMapping(path = "/webcam")
public class WebcamController {

    private WebcamService webcamService;
    private ImageFilters imageFilters;

    @GetMapping(produces =  MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> get() {
        return webcamService.getImage();
    }

    @PostMapping("start")
    public void start() {
        webcamService.start();
    }

    @PostMapping("stop")
    public void stop() {
        webcamService.stop();
    }

    @PostMapping("write")
    public void write() {
        webcamService.subscribe(this::writeImage, e -> e.printStackTrace());
    }

    private void writeImage(Mat frame)  {
        try {
            BufferedImage image = imageFilters.matToBufferedImage(frame);
            File file = new File("/tmp/cap-" + System.currentTimeMillis() + ".jpg");
            ImageIO.write(image, "jpg", file);
            log.info("writen " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
