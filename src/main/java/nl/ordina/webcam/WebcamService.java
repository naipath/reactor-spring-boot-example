package nl.ordina.webcam;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WebcamService {

    private final ImageFilters imageFilters;
    private final VideoCapture videoCapture;
    private final Flux<Mat> webcamStream;

    public WebcamService() {
        imageFilters = new ImageFilters();
        videoCapture = new VideoCapture(0);
        webcamStream = Flux.intervalMillis(80)
                .takeWhile(now -> videoCapture.isOpened())
                .map(this::grab);
    }

    public Mono<byte[]> getImage() {
        return Mono
                .just(grab(System.currentTimeMillis()))
                .map(imageFilters::resize320180)
                .map(this::encode);
    }

    public Flux<Mat> getWebcamStream() {
        return webcamStream;
    }

    private Mat grab(long now) {
        Mat image = new Mat();
        videoCapture.read(image);
        return image;
    }

    private byte[] encode(Mat image) {
        MatOfByte data = new MatOfByte();
        Highgui.imencode(".jpg", image, data);
        return data.toArray();
    }
}
