package nl.ordina.webcam;

import java.awt.image.BufferedImage;
import java.io.Closeable;

/**
 * Created by steven on 15-01-17.
 */
public interface WebStream<T> extends Closeable {
    void init();

    void start();

    void stop();

    void record(T image);
}
