package nl.ordina.webcam;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by steven on 25-12-16.
 */
@Component
public class ImageFilters {

    private static final Scalar BLUE = new Scalar(0, 0, 255);
    private static final Scalar GREEN = new Scalar(0, 255, 0);
    private Mat edgeKernel;
    private CascadeClassifier facedetectionClassifier;
    private CascadeClassifier eyedetectionClassifier;

    public ImageFilters() {
        facedetectionClassifier = new CascadeClassifier("haarcascade_frontalface_default.xml");
        eyedetectionClassifier = new CascadeClassifier("haarcascade_eye.xml");
        edgeKernel = createMat(3,3,CV_64FC1, new double[] {
                -1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f,
                -1.0f, 0.0f, 1.0f
        });
    }

    public Mat resize640480(Mat image) {
        resize(image, image, new Size(640, 480), 0, 0, INTER_CUBIC);
        return image;
    }

    public Mat resize320180(Mat image) {
        resize(image, image, new Size(320,180), 0, 0, INTER_CUBIC);
        return image;
    }

    public Mat edgeDetect(Mat image) {
        Imgproc.filter2D(image, image, -1, edgeKernel);
        return image;
    }

    public Mat invert(Mat image) {
        bitwise_not(image, image);
        return image;
    }

    public Mat copy(Mat image) {
        return image.clone();
    }

    public Mat gray(Mat image) {
        cvtColor(image, image, COLOR_BGR2GRAY);
        return image;
    }

    public Mat fromBGR2ToYUV_I420(Mat image) {
        Mat dest = Mat.eye(image.rows(), image.cols(), image.type());
        cvtColor(image, dest, Imgproc.COLOR_BGR2YUV_I420);
        return dest;
    }

    public MatOfRect facedetection(Mat image) {
        MatOfRect objects = new MatOfRect();
        facedetectionClassifier.detectMultiScale(image, objects);
        return objects;
    }

    public List<Tuple2<Rect,MatOfRect>> eyedetection(Mat image, MatOfRect faces) {
        List<Tuple2<Rect,MatOfRect>> eyeList = new ArrayList<>();
        faces.toList().forEach(face -> {
            MatOfRect eyes = new MatOfRect();
            Mat faceImage = image.submat(face);
            eyedetectionClassifier.detectMultiScale(faceImage, eyes);
            eyeList.add(Tuples.of(face,eyes));
        });
        return eyeList;
    }

    public Mat drawFaces(Mat image, List<Tuple2<Rect,MatOfRect>> facesWithEyes ) {
        facesWithEyes.forEach( faceWithEyes -> {
            Rect face = faceWithEyes.getT1();
            MatOfRect eyes = faceWithEyes.getT2();
            rectangle(image, face.tl(), face.br(), BLUE);

            List<Rect> listOfEyesRelativeToFace = eyes.toList().stream()
                    .map(relativeTo(face))
                    .collect(Collectors.toList());

            MatOfRect eyesRelativeToFace = matOfRectFromList(listOfEyesRelativeToFace);
            drawRectangles(image, eyesRelativeToFace, GREEN);
        });
        return image;
    }

    public Mat drawRectangles(Mat image, MatOfRect objects, Scalar color) {
        objects.toList().forEach(rect -> {
            rectangle(image, rect.tl(), rect.br(), color);
        });
        return image;
    }

    public Mat hogDescriptor(Mat image) {
        HOGDescriptor hogDescriptor = new HOGDescriptor();
        hogDescriptor.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
        MatOfRect objects = new MatOfRect();
        MatOfDouble weights = new MatOfDouble();
        hogDescriptor.detectMultiScale(image, objects, weights);
        objects.toList().forEach(rect -> {
            rectangle(image, rect.tl(), rect.br(), new Scalar(255, 0, 0));
        });
        return image;
    }

    public Image toImage(BufferedImage image) {
        return SwingFXUtils.toFXImage(image, null);
    }

    public BufferedImage matToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    private MatOfRect matOfRectFromList(List<Rect> listOfRect) {
        MatOfRect matOfRect = new MatOfRect();
        matOfRect.fromList(listOfRect);
        return matOfRect;
    }

    private Function<Rect,Rect> relativeTo(Rect parent) {
        return rect -> {
            Point p = new Point(rect.tl().x + parent.tl().x,rect.tl().y + parent.tl().y);
            Size s = rect.size();
            Rect rectInImage = new Rect(p, s);
            return rectInImage;
        };
    }

    private Mat createMat(int rows, int cols, int type, double[] data) {
        Mat mat = new Mat(rows, cols, type);
        int p = 0;
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] cell = new double[mat.channels()];
                for (int k = 0; k < mat.channels(); k++) {
                    cell[k] = data[p++];
                }
                mat.put(i,j, cell);
            }
        }
        return mat;
    }
}
