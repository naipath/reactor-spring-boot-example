package nl.ordina.webcam;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.rectangle;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by steven on 25-12-16.
 */
public class ImageFilters {

    private Mat edgeKernel;
    private CascadeClassifier facedetectionClassifier;

    public ImageFilters() {
        facedetectionClassifier = new CascadeClassifier("haarcascade_frontalface_default.xml");
        edgeKernel = createMat(3,3,CV_64FC1, new double[] {
                -1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f,
                -1.0f, 0.0f, 1.0f
        });
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

    public Mat gray(Mat image) {
        cvtColor(image, image, COLOR_BGR2GRAY);
        return image;
    }

    public Mat facedetection(Mat image) {
        MatOfRect objects = new MatOfRect();
        facedetectionClassifier.detectMultiScale(image, objects);
        objects.toList().forEach(rect -> {
            rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0));
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
