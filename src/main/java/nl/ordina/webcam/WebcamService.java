package nl.ordina.webcam;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.opencv.imgcodecs.Imgcodecs.imencode;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.resize;

@Service
public class WebcamService {

    private ImageFilters imageFilters;
    private VideoCapture videoCapture;
    private Flux<Mat> webcamStream;
    private WebStreamUDP webStream;


//    @PostConstruct
    public void initialize() {
        webStream = new WebStreamUDP();
        webStream.init();
        webStream.start();
        imageFilters = new ImageFilters();
        videoCapture = new VideoCapture(0);

        Function<Mat, Mat> faceDetectionFlow = image -> {
            Mat grey = imageFilters.copy(image);
            imageFilters.gray(grey);
            MatOfRect faces = imageFilters.facedetection(grey);
            List<Tuple2<Rect, MatOfRect>> facesAndEyes = imageFilters.eyedetection(grey, faces);
            imageFilters.drawFaces(image, facesAndEyes);
            return image;
        };

        webcamStream = Flux.intervalMillis(100)
                .takeWhile(now -> videoCapture.isOpened())
                .map(this::grab)
                .onBackpressureBuffer(20, BufferOverflowStrategy.DROP_OLDEST)
                .onBackpressureDrop(mat -> {
                    System.out.println("dropped");
                })
                .filter(image -> image.rows() > 0 && image.cols() > 0)
                .map(imageFilters::resize640480)
                .map(faceDetectionFlow);

        webcamStream.subscribe(image -> {
                webStream.record(image);
            });
    }

    @PreDestroy
    public void close() {
        try {
            webStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(Subscriber<Mat> subscriber) {
        webcamStream.subscribe(subscriber);
    }

    public void subscribe(Consumer<? super Mat> onNext, Consumer<? super Throwable> onError) {
        webcamStream.subscribe(onNext, onError);
    }

    public Mono<byte[]> getImage() {

        Mono<Mat> grab = Mono.just(grab(System.currentTimeMillis()));
        Mono<Mat> grey = grab
                .map(imageFilters::copy)
                .map(imageFilters::gray);

        return grey
                .map(imageFilters::facedetection)
                .and(grey)
                .map(tuple -> imageFilters.eyedetection(tuple.getT2(), tuple.getT1()))
                .and(grab)
                .map(tuple -> imageFilters.drawFaces(tuple.getT2(), tuple.getT1()))
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
        imencode(".jpg", image, data);
        return data.toArray();
    }

    public void start() {
        if(!videoCapture.isOpened()) {
            videoCapture.open(0);
        }
    }

    public void stop() {
        if(videoCapture.isOpened()) {
            videoCapture.release();
        }
    }
}
