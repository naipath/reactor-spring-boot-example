package nl.ordina.webcam;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import reactor.core.publisher.Flux;

public class WebcamCapture {

    public Flux<Mat> createCapture(long millis) {
        ImageFilters imageFilters = new ImageFilters();
        VideoCapture videoCapture = new VideoCapture(0);
        return Flux.intervalMillis(millis)
                .takeWhile(now -> videoCapture.isOpened())
                .map(now -> grab(videoCapture))
                .filter(this::isValidImage)
                .map(imageFilters::resize640480)
                .map(imageFilters::detectAndDrawFaces);
    }

    private boolean isValidImage(Mat image) {
        return image.rows() > 0 && image.cols() > 0;
    }

    private Mat grab(VideoCapture videoCapture) {
        Mat image = new Mat();
        videoCapture.read(image);
        return image;
    }

}
