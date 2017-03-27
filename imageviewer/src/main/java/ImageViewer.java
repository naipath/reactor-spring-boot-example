import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ImageViewer extends Application {

    private ImageView imageView;

    public void start(final Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        Scene s = new Scene(root, 400, 400, Paint.valueOf("blue"));

        final Canvas canvas = new Canvas(300, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Paint.valueOf("blue"));
        gc.fillRect(10, 10, 300, 300);

        imageView = new ImageView("https://cdn0.iconfinder.com/data/icons/toys/256/teddy_bear_toy_6.png");

        root.getChildren().addAll(canvas, imageView);

        primaryStage.setScene(s);
        primaryStage.show();

//        listenToConsumer();
        listenToFlux();
    }

    public static void main(String[] args) {
        launch(args);

    }

    private void listenToFlux() {
        ImageConsumer consumer = new ImageConsumer();
        Flux<byte[]> flux = consumer.create();
        flux.subscribeOn(Schedulers.elastic()).subscribe(this::updateImage);
    }

    private void listenToConsumer() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                ImageConsumer consumer = new ImageConsumer();
                consumer.listen(ImageViewer.this::updateImage);
            }
        });
    }


    private void updateImage(byte[] byteArray) {
        System.out.println("received byte array");
        Platform.runLater( () -> {
            InputStream in = new ByteArrayInputStream(byteArray);
            try {
                BufferedImage image = ImageIO.read(in);
                imageView.setImage(SwingFXUtils.toFXImage(image, null));
                System.out.println("updated image");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
