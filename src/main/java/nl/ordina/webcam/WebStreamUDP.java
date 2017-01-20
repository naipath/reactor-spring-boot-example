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
import static org.bytedeco.javacpp.swscale.SWS_BICUBIC;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

/**
 * Created by steven on 02-01-17.
 */
@Component
public class WebStreamUDP implements WebStream<Mat> {

    final String filename = "udp://0.0.0.0:12345";
    //        final String filename = "/tmp/tst.flv";
    final int bitRate = 2 * 1024 * 1024;

    final int width = 640;
    final int height = 480;
    final int duration = 10;
    final int sws_flags = SWS_BICUBIC;
    final int pixelFormatVideoStream = AV_PIX_FMT_YUV420P;//AV_PIX_FMT_YUV420P;
    final int pixelFormatOpenCVImage = AV_PIX_FMT_BGR24;
//    final int pixelFormat = AV_PIX_FMT_0RGB;//AV_PIX_FMT_YUV420P;
//    final int codec = AV_CODEC_ID_H264;
    final int codec = AV_CODEC_ID_MPEG2VIDEO;
//    final int codec = AV_CODEC_ID_BFI;
//    final int codec = AV_CODEC_ID_H265;
//    final int codec = AV_CODEC_ID_RAWVIDEO;


    final String ext = "mpegts";
//    final String ext = "rawvideo";
//    final String ext = "yuv4mpegpipe";

//    final String ext = "h264_videotoolbox";
    final avutil.AVRational timbase;


    private AVOutputFormat outputFormat;
    private AVFormatContext avFormatContext;
    private AVCodec videoCodec;
    private AVCodecContext avCodecContext;
    private AVStream stream;
    avutil.AVDictionary options = new avutil.AVDictionary();
    private AVFrame openCVImageFrame;
    private AVFrame videoFrame;
    private AVIOContext avioContext;

    private final int imageBufferOpenCVSize = width * height * 3;
    private final byte[] imageByteArrayOpenCV = new byte[(int) imageBufferOpenCVSize];


    public WebStreamUDP() {
        timbase = new avutil.AVRational();
        timbase.num(1);
        timbase.den(15);
    }

    @Override
    public void init() {

        int ret;

        av_register_all();
        avformat_network_init();
//
//        //set output format
        outputFormat = av_guess_format(ext, null, null);
//
//        //alloc outputmedia context
        avFormatContext = new avformat.AVFormatContext();
        avformat_alloc_output_context2(avFormatContext, outputFormat, ext, filename);
//
//        //add video stream
        videoCodec = avcodec_find_encoder(codec);
//
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


//        if ( (avFormatContext.flags() & AVFMT_GLOBALHEADER) != 0) {
//            avFormatContext.flags( avFormatContext.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
//        }

//        av_set_options_string(options, "preset=superfast;profile=baseline;tune=zerolatency;vbv-maxrate=5000;vbv-bufsize=1;slice-max-size=1500;keyint=60", "=", ";");
        av_set_options_string(options, "preset=superfast;profile=baseline;tune=zerolatency;", "=", ";");

        //h264 options
//        av_set_options_string(options, "x264opts crf=20:vbv-maxrate=3000:vbv-bufsize=50:intra-refresh=1:slice-max-size=1500:keyint=30:ref=1"," ", ";");

//        //open video
        ret = avcodec_open2(avCodecContext, videoCodec, options);
//
//        //alloc frame
        openCVImageFrame = av_frame_alloc();
        if(openCVImageFrame == null) {
            System.out.println("Error create reusable frame");
            return;
        }
        openCVImageFrame.width(width);
        openCVImageFrame.height(height);
        openCVImageFrame.format(pixelFormatOpenCVImage);
        openCVImageFrame.pts(0);
        ret = av_frame_get_buffer(openCVImageFrame, 32);
        if(ret < 0) {
            System.err.println("Kan buffer niet alloceren");
        }

        videoFrame = av_frame_alloc();
        if(videoFrame == null) {
//            //TODO: error handling
            System.out.println("Error create reusable frame");
            return;
        }
        videoFrame.width(width);
        videoFrame.height(height);
        videoFrame.format(pixelFormatVideoStream);
        videoFrame.pts(0);
        ret = av_frame_get_buffer(videoFrame, 32);
        if(ret < 0) {
            System.err.println("Kan buffer niet alloceren");
        }
//        //write format info
//        //TODO: writes to stdout
        av_dump_format(avFormatContext, 0, filename, 1);
//
//        //open the file
        avioContext = new AVIOContext();
        ret = avio_open(avioContext, filename, AVIO_FLAG_WRITE);
        avFormatContext.pb(avioContext);

    }

    @Override
    public void start() {
        //write header
        int ret;
        ret = avformat_write_header(avFormatContext, options);
    }

    @Override
    public void stop() {
        av_write_trailer(avFormatContext);
    }


    @Override
    public void record(Mat image) {

        swscale.SwsContext imgConvertCtx = null;
            copyImageToFrame(image, openCVImageFrame);
            if(imgConvertCtx == null) {
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
                System.out.println("write frame - " + videoFrame.pts());
            }
            av_free_packet(packet);
        }

    private void copyImageToFrame(Mat image, AVFrame picture) {
        image.get(0, 0, imageByteArrayOpenCV);
        AVPicture avPicture = new AVPicture(picture);
        avpicture_fill(avPicture, imageByteArrayOpenCV, pixelFormatOpenCVImage, width, height);
    }

    @Override
    public void close() throws IOException {
        av_frame_free(openCVImageFrame);
        avcodec_close(avCodecContext);
        avcodec_free_context(avCodecContext);
        avformat_free_context(avFormatContext);
    }
}
