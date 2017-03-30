package nl.ordina.webcam;

import nl.ordina.messages.ImagePublisherService;
import org.opencv.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.opencv.imgcodecs.Imgcodecs.imencode;
import static org.opencv.imgproc.Imgproc.resize;

@Service
public class WebcamService {

    public static final int FREQUENCY = 100;
    @Autowired
    private ImagePublisherService imagePublisherService;

    private Flux<Mat> webcamFlux;
    private WebStreamUDP webStream;
    private Disposable publishImageSubscription;
    private Disposable webStreamSubscription;
    private Flux<SenderResult<Integer>> publishImageSenderResult;

    @PostConstruct
    public void initialize() {

        webStream = new WebStreamUDP();
        webStream.init();
        webStream.start();
        WebcamCapture webcamCapture = new WebcamCapture();
        webcamFlux = webcamCapture.createCapture(FREQUENCY);
        publishImageSenderResult = imagePublisherService.send(webcamFlux.map(this::encode));
    }

    @PreDestroy
    public void close() {
        try {
            webStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe() {

        if(webStreamSubscription == null || webStreamSubscription.isDisposed()) {
            publishImageSubscription = publishImageSenderResult.subscribe();
            webStreamSubscription = webcamFlux.subscribe(webStream::record);
        }
    }

    public void unsubscribe() {
        if(webStreamSubscription != null && !webStreamSubscription.isDisposed()) {
            publishImageSubscription.dispose();
            webStreamSubscription.dispose();
        }
    }

    public Mono<byte[]> getImage() {
        return webcamFlux
                .next()
                .map(this::encode);
    }

    public byte[] encode(Mat image) {
        MatOfByte data = new MatOfByte();
        imencode(".jpg", image, data);
        return data.toArray();
    }

}
