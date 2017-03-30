package nl.ordina.webcam;


import org.bytedeco.javacpp.*;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avcodec.avcodec_open2;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avformat.AVIO_FLAG_WRITE;
import static org.bytedeco.javacpp.avformat.avio_open;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.swscale.*;

/**
 * Created by steven on 02-01-17.
 */
@Component
public class WebStreamUDP {

    private static final String filename = "udp://0.0.0.0:12345";
    private static final int bitRate = 2 * 1024 * 1024;
    private static final int width = 640;
    private static final int height = 480;
    private static final int duration = 10;
    private static final int sws_flags = SWS_BICUBIC;
    private static final int pixelFormatVideoStream = AV_PIX_FMT_YUV420P;//AV_PIX_FMT_YUV420P;
    private static final int pixelFormatOpenCVImage = AV_PIX_FMT_BGR24;
    private static final int codec = AV_CODEC_ID_MPEG2VIDEO;
    private static final String ext = "mpegts";

    private final avutil.AVRational timbase;
    private AVOutputFormat outputFormat;
    private AVFormatContext avFormatContext;
    private AVCodec videoCodec;
    private AVCodecContext avCodecContext;
    private AVStream stream;
    private avutil.AVDictionary options = new avutil.AVDictionary();
    private AVFrame openCVImageFrame;
    private AVFrame videoFrame;
    private AVIOContext avioContext;
    private final int imageBufferOpenCVSize = width * height * 3;
    private final byte[] imageByteArrayOpenCV = new byte[(int) imageBufferOpenCVSize];
    private swscale.SwsContext imgConvertCtx = null;

    public WebStreamUDP() {
        timbase = new avutil.AVRational();
        timbase.num(1);
        timbase.den(15);
    }

    public void init() {
        int ret;
        av_register_all();
        avformat_network_init();
        outputFormat = av_guess_format(ext, null, null);
        avFormatContext = new avformat.AVFormatContext();
        avformat_alloc_output_context2(avFormatContext, outputFormat, ext, filename);
        videoCodec = avcodec_find_encoder(codec);
        avCodecContext = avcodec_alloc_context3(videoCodec);
        stream = avformat_new_stream(avFormatContext, videoCodec);
        avCodecContext.time_base(timbase);
        avCodecContext.gop_size(10);
        avCodecContext.max_b_frames(1);
        avCodecContext.pix_fmt(pixelFormatVideoStream);
        avCodecContext.width(width);
        avCodecContext.height(height);
        avCodecContext.bit_rate(bitRate);
        avCodecContext.framerate(timbase);
        stream.codec(avCodecContext);
        stream.time_base(timbase);
        stream.duration(duration);

        av_set_options_string(options, "preset=superfast;profile=baseline;tune=zerolatency;", "=", ";");
        ret = avcodec_open2(avCodecContext, videoCodec, options);
//
//        //alloc frame
        allocCVImageFrame();
        openCVImageFrame.width(width);
        openCVImageFrame.height(height);
        openCVImageFrame.format(pixelFormatOpenCVImage);
        openCVImageFrame.pts(0);
        ret = av_frame_get_buffer(openCVImageFrame, 32);
        if(ret < 0) {
            throw new RuntimeException("Kan buffer niet alloceren");
        }

        videoFrame = av_frame_alloc();
        if(videoFrame == null) {
            throw new RuntimeException("Error create reusable frame");
        }
        videoFrame.width(width);
        videoFrame.height(height);
        videoFrame.format(pixelFormatVideoStream);
        videoFrame.pts(0);

        ret = av_frame_get_buffer(videoFrame, 32);
        if(ret < 0) {
            throw new RuntimeException("Kan buffer niet alloceren");
        }
//        //write format info
//        //TODO: writes to stdout
        av_dump_format(avFormatContext, 0, filename, 1);
//
//        //open the file
        avioContext = new AVIOContext();
        ret = avio_open(avioContext, filename, AVIO_FLAG_WRITE);
        avFormatContext.pb(avioContext);

        DoublePointer params = new DoublePointer();
        imgConvertCtx = sws_getContext(
                width,
                height,
                pixelFormatOpenCVImage,
                avCodecContext.width(),
                avCodecContext.height(),
                avCodecContext.pix_fmt(),
                sws_flags,
                null,
                null,
                params
        );


    }

    private void allocCVImageFrame() {
        openCVImageFrame = av_frame_alloc();
        if(openCVImageFrame == null) {
            throw new RuntimeException("Error create reusable frame");
        }
    }

    public void start() {
        //write header
        int ret;
        ret = avformat_write_header(avFormatContext, options);
    }

    public void stop() {
        av_write_trailer(avFormatContext);
    }


    public void record(Mat image) {

        copyImageToFrame(image, openCVImageFrame);
        sws_scale(
                imgConvertCtx,
                openCVImageFrame.data(),
                openCVImageFrame.linesize(),
                0,
                avCodecContext.height(),
                videoFrame.data(),
                videoFrame.linesize()
        );


        long nextPts = av_rescale_q(1, avCodecContext.time_base(), stream.time_base());
        videoFrame.pts(videoFrame.pts() + nextPts);
        AVPacket packet = new AVPacket();
        av_init_packet(packet);
        int[] gotPacket = new int[1];
        avcodec_encode_video2(avCodecContext, packet, videoFrame, gotPacket);
        if(gotPacket[0] == 1) {
            packet.stream_index(stream.index());
            packet.dts(videoFrame.pts());
            packet.pts(videoFrame.pts());
            av_write_frame(avFormatContext, packet);
        }
        av_free_packet(packet);
    }

    private void copyImageToFrame(Mat image, AVFrame picture) {
        image.get(0, 0, imageByteArrayOpenCV);
        AVPicture avPicture = new AVPicture(picture);
        avpicture_fill(avPicture, imageByteArrayOpenCV, pixelFormatOpenCVImage, width, height);
    }

    public void close() throws IOException {
        sws_freeContext(imgConvertCtx);
        av_frame_free(openCVImageFrame);
        avcodec_close(avCodecContext);
        avcodec_free_context(avCodecContext);
        avformat_free_context(avFormatContext);
    }
}
