package nl.ordina.webcam;


import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by steven on 02-01-17.
 */
@Component
public class WebStreamRTMP implements WebStream<BufferedImage> {

    Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
    FFmpegFrameRecorder recorder;

    @Override
    public void init() {
        recorder = new FFmpegFrameRecorder("rtmp://localhost/live", 320,240);
        recorder.setInterleaved(true);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
//        recorder.setVideoBitrate(2_000_000);
        recorder.setVideoBitrate(128 * 1024);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MJPEG);
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_RAWVIDEO);
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P10);
//        recorder.setFormat("mjpeg");
        recorder.setFormat("flv");
//        recorder.setFormat("raw");
//        recorder.setFormat("mjpeg");
        // FPS (frames per second)
        recorder.setFrameRate(15);
        // Key frame interval, in our case every 2 seconds -> 30 (fps) * 2 = 60
        // (gop length)
        recorder.setGopSize(30);
    }

    @Override
    public void start() {
        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            recorder.stop();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void record(BufferedImage bufferedImage) {
        Frame frame = java2DFrameConverter.getFrame(bufferedImage);
        try {
            recorder.record(frame);
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
    }
}
