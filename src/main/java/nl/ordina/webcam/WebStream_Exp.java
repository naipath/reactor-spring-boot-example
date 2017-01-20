package nl.ordina.webcam;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.ByteBufferSeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.AWTUtil;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.DatagramChannel;

import static com.sun.javafx.tools.resource.DeployResource.Type.data;
import static java.lang.Math.PI;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.swscale.SWS_BICUBIC;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

/**
 * Created by steven on 02-01-17.
 */
public class WebStream_Exp {

    public static void pipeImageToVideoStream(BufferedImage image) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-f", "image2pipe", "-codec", "mjpeg" ,"-i", "pipe:0", "/tmp/out.avi", "<&0");
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);

        Process process = processBuilder.start();
        if(process.isAlive()) {
            System.out.println("alive");
        }

//        BufferedReader readerErrorStream = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getErrorStream())));
//        String line;
//        while ( (line= readerErrorStream.readLine()) != null) {
//            System.out.println(readerErrorStream.readLine());
//        }
//        BufferedReader readerInputStream = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
//        while ( (line= readerInputStream.readLine()) != null) {
//            System.out.println(readerInputStream.readLine());
//        }

//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        ImageIO.write( image, "jpg", stream );
//        stream.flush();
//        byte[] buffer = stream.toByteArray();
//        stream.close();


        for(int i=0; i < 10 && process.isAlive(); i++) {
            ImageIO.write(image, "jpg", process.getOutputStream());
            process.getOutputStream().flush();

//            while ( (line= readerInputStream.readLine()) != null) {
//                System.out.println(readerInputStream.readLine());
//            }

            Thread.sleep(100);
        }
        process.getOutputStream().close();


        process.waitFor();

    }

    private static void encodeToH264(BufferedImage image, DatagramChannel datagramChannel) throws IOException {

        H264Encoder h264Encoder = new H264Encoder();
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        MP4Muxer muxer = new MP4Muxer(new ByteBufferSeekableByteChannel(buffer), Brand.MP4);
        FramesMP4MuxerTrack outTrack = muxer.addTrack(TrackType.VIDEO, 25);
//        MP4Packet mp4Packet = new MP4Packet();
//        outTrack.addFrame(mp4Packet);
        //new MP4Packet(result, frameNo, 25, 1, frameNo, true, null, frameNo, 0)

        Picture picture = AWTUtil.fromBufferedImage(image);
        Picture encode = Picture.create(picture.getWidth(), picture.getHeight(), h264Encoder.getSupportedColorSpaces()[0]);
        Transform transform = ColorUtil.getTransform(ColorSpace.RGB, h264Encoder.getSupportedColorSpaces()[0]);
        transform.transform(picture, encode);
//        h264Encoder.encodeFrame(encode, buffer);




//        SequenceEncoder enc = new SequenceEncoder(new File("tst.mp4"));
        // GOP size will be supported in 0.2
//        enc.getEncoder().setKeyInterval(25);
        for(;;) {
//            enc.encodeImage(image);
            ByteBuffer out = h264Encoder.encodeFrame(encode, buffer);
            int buffersize = datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF);
            ByteBuffer view = out.slice();
            System.out.println("write buffer to datagram");

            for (int start = out.position(), end = out.limit(), stride = buffersize;
                 start != end;
                 start = view.limit()) {
                view.position(start);
                view.limit(start + Math.min(end - start, stride));
                System.out.println("view " +  view.position() + " " + view.limit());
                try {
                    int b = datagramChannel.write(view);
                    System.out.println(b + " bytes written");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
//        enc.finish();
    }


    private static void record(BufferedImage bufferedImage) {

        final String filename = "udp://0.0.0.0:12345";
//        final String filename = "/tmp/tst.mp4";
        final int bitRate = 1 *  512 * 1024;
        final int width = 640;
        final int height = 480;
        final int duration = 1;
        AVRational timbase = new AVRational();
        timbase.num(1);
        timbase.den(25);
//        final int pixelFormat = AV_PIX_FMT_YUVJ420P;//AV_PIX_FMT_YUV420P;
//        final int codec = AV_CODEC_ID_MJPEG;
//        final String ext = "mjpeg";
        final int sws_flags = SWS_BICUBIC;

        final int pixelFormat = AV_PIX_FMT_YUV420P;//AV_PIX_FMT_YUV420P;
//        final int codec = AV_CODEC_ID_H264;
        final int codec = AV_CODEC_ID_MPEG2VIDEO;
        final String ext = "mpeg";
//        final String ext = "mp4";
//        final String ext = "avi";

//        final int pixelFormat = AV_PIX_FMT_YUV420P;//AV_PIX_FMT_YUV420P;
//        final int codec = AV_CODEC_ID_MPEG2TS;
//        final String ext = "mpeg";

        int ret;


        opencv_videoio.VideoCapture capture = new opencv_videoio.VideoCapture(0);


//        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
//        Frame frame = java2DFrameConverter.getFrame(bufferedImage);

        av_register_all();
        avformat_network_init();

        //set output format
        AVOutputFormat outputFormat = av_guess_format(ext, null, null);


        //alloc outputmedia context
        avformat.AVFormatContext avFormatContext = new avformat.AVFormatContext();
        avformat_alloc_output_context2(avFormatContext,outputFormat, ext, filename);

        //add video stream
        avcodec.AVCodec videoCodec = avcodec_find_encoder(codec);

        AVCodecContext avCodecContext = avcodec_alloc_context3(videoCodec);
        AVStream stream = avformat_new_stream(avFormatContext, videoCodec);

        avCodecContext.time_base(timbase);
        avCodecContext.gop_size(15);
//        avCodecContext.max_b_frames(2);
        avCodecContext.pix_fmt(pixelFormat);
        avCodecContext.width(width);
        avCodecContext.height(height);
        avCodecContext.bit_rate(bitRate);
        avCodecContext.compression_level(0);
        avCodecContext.rc_max_rate(1 * 1024 * 1024);
        avCodecContext.rc_min_rate(1 * 1024);
        avCodecContext.framerate(timbase);
        avCodecContext.rc_buffer_size(2 * 1024 * 1024);


        stream.codec(avCodecContext);
        stream.time_base(timbase);
        stream.duration(duration);
//        stream.codecpar().bit_rate(bitRate);
//        stream.codecpar().width(width);
//        stream.codecpar().height(height);





        if ( (avFormatContext.flags() & AVFMT_GLOBALHEADER) != 0) {
            avFormatContext.flags( avFormatContext.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
        }

        //? gop?
        //? max_b_frames?

        //set options
        avutil.AVDictionary options = new avutil.AVDictionary();
//        av_dict_set(options, "profile", "baseline", 0);
//        av_dict_set(options, "tune", "zerolatency", 0);

        //open video
        ret = avcodec_open2(avCodecContext, videoCodec, options);

        //alloc frame
        avutil.AVFrame picture = av_frame_alloc();
        if(picture == null) {
            System.out.println("Error create reusable frame");
            return;
        }
        picture.width(width);
        picture.height(height);
        picture.format(pixelFormat);
        picture.pts(0);
        ret = av_frame_get_buffer(picture, 32);
        if(ret < 0) {
            System.err.println("Kan buffer niet alloceren");
        }
//        AVFrameSideData avFrameSideData = new AVFrameSideData();
//        av_frame_new_side_data(picture, AVFrameSideDataType)
//        picture.side_data(0, avFrameSideData);

        avutil.AVFrame tmpPicture = av_frame_alloc();
        if(tmpPicture == null) {
            System.out.println("Error create reusable frame");
            return;
        }
        tmpPicture.width(width);
        tmpPicture.height(height);
        tmpPicture.format(AV_PIX_FMT_YUV420P);
        tmpPicture.pts(0);
        ret = av_frame_get_buffer(tmpPicture, 32);
        if(ret < 0) {
            System.err.println("Kan buffer niet alloceren");
        }



        //write format info
        av_dump_format(avFormatContext, 0, filename, 1);

        //open the file
        AVIOContext avioContext = new AVIOContext();
        ret = avio_open(avioContext, filename, AVIO_FLAG_WRITE);
        avFormatContext.pb(avioContext);

        //write header
        ret = avformat_write_header(avFormatContext, options);

        swscale.SwsContext imgConvertCtx = null;
        //write frame
        for(int next=0; next < 1000; next++) {

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            //alloc reuseable frame

            capture.grab();
            opencv_core.Mat mat = new opencv_core.Mat();
            capture.retrieve(mat);
            resize(mat, mat, new opencv_core.Size(avCodecContext.width(), avCodecContext.height()));
            imshow("/tmp/grab.png", mat);
//            cvtColor(mat, mat, CV_BGR2YCrCb);



            int numBytes = avpicture_get_size(AV_PIX_FMT_BGR24, width, height);
//            Pointer frame2_buffer = av_malloc(numBytes);

            avutil.AVFrame temp_rgb_frame = av_frame_alloc();
            if(tmpPicture == null) {
                System.out.println("Error create reusable frame");
                return;
            }
            temp_rgb_frame.width(width);
            temp_rgb_frame.height(height);
            temp_rgb_frame.format(AV_PIX_FMT_BGR24);

//            temp_rgb_frame.pts(0);

            AVPicture tmpPic = new AVPicture(temp_rgb_frame);

//            avpicture_fill(tmpPic, frame2_buffer.asByteBuffer(), AV_PIX_FMT_0RGB, width, height);
            avpicture_fill(tmpPic, mat.data(), AV_PIX_FMT_BGR24, width, height);
//            avpicture_fill(new AVPicture(picture), mat.data(), pixelFormat, avCodecContext.width(), avCodecContext.height());

            long nextPts = av_rescale_q(1, avCodecContext.time_base(), stream.time_base());
            picture.pts(picture.pts() + nextPts);
            temp_rgb_frame.pts(picture.pts());



//            fill_yuv_image(picture, next, width, height);

            if(imgConvertCtx == null) {
                DoublePointer params = new DoublePointer();
                imgConvertCtx = sws_getContext(
                        width,
                        height,
                        AV_PIX_FMT_BGR24,
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
                    temp_rgb_frame.data(),
                    temp_rgb_frame.linesize(),
                    0,
                    avCodecContext.height(),
                    tmpPicture.data(),
                    tmpPicture.linesize()
            );
            tmpPicture.pts(picture.pts());

//            avcodec_send_frame(avCodecContext, picture);
//            avcodec_flush_buffers(avCodecContext);
            AVPacket packet = new AVPacket();
            av_init_packet(packet);
            int[] gotPacket = new int[1];
            avcodec_encode_video2(avCodecContext, packet, tmpPicture, gotPacket);
            if(gotPacket[0] == 1) {
                packet.stream_index(stream.index());
//                packet.size(tmpPicture.sizeof());
                packet.dts(tmpPicture.pts());
                packet.pts(tmpPicture.pts());
                av_write_frame(avFormatContext, packet);
//                av_interleaved_write_frame(avFormatContext, packet);
                System.out.println(next + " - " + tmpPicture.pts());
            }
            av_free_packet(packet);
        }

        av_write_trailer(avFormatContext);

        avformat_flush(avFormatContext);
        av_frame_free(tmpPicture);
        av_frame_free(picture);
        avcodec_close(avCodecContext);
        avformat_free_context(avFormatContext);
    }

    private static void grabwebcam(AVFrame picture, int width, int height) {


    }


    static private void fill_yuv_image(AVFrame pict, int frame_index,  int width, int height) {
        int x, y, i, ret;
    /* when we pass a frame to the encoder, it may keep a reference to it
     * internally;
     * make sure we do not overwrite it here
     */
        ret = av_frame_make_writable(pict);
        if (ret < 0) {
            return;
        }
        i = frame_index;
    /* Y */
        for (y = 0; y < height; y++)
            for (x = 0; x < width; x++) {
//                pict->data[0][y * pict->linesize[0] + x] = x + y + i * 3;
                pict.data(0).position(y * pict.linesize(0) + x).put((byte) (x + y + i * 3));
            }
    /* Cb and Cr */
        for (y = 0; y < height / 2; y++) {
            for (x = 0; x < width / 2; x++) {
//                pict->data[1][y * pict->linesize[1] + x] = 128 + y + i * 2;
//                pict->data[2][y * pict->linesize[2] + x] = 64 + x + i * 5;
                pict.data(1).position(y * pict.linesize(1) + x).put((byte) (128 + y + i * 2));
                pict.data(2).position(y * pict.linesize(2) + x).put((byte) (64 + x + i * 5));
            }
        }
    }



    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedImage bufferedImage = ImageIO.read(new File("example.jpg"));
        record(bufferedImage);

//        DatagramChannel datagramChannel = DatagramChannel.open();
//        datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, 65507);
//        datagramChannel.configureBlocking(false);
//        datagramChannel.connect(new InetSocketAddress(12345));

//        encodeToH264(bufferedImage, datagramChannel);
//        datagramChannel.close();


//        pipeImageToVideoStream(bufferedImage);
    }

//
//    static void SaveFrame(AVFrame pFrame, int width, int height, int iFrame)
//            throws IOException {
//        // Open file
//        OutputStream stream = new FileOutputStream("frame" + iFrame + ".ppm");
//
//        // Write header
//        stream.write(("P6\n" + width + " " + height + "\n255\n").getBytes());
//
//        // Write pixel data
//        BytePointer data = pFrame.data(0);
//        byte[] bytes = new byte[width * 3];
//        int l = pFrame.linesize(0);
//        for(int y = 0; y < height; y++) {
//            data.position(y * l).get(bytes);
//            stream.write(bytes);
//        }
//
//        // Close file
//        stream.close();
//    }
//
//    public static void main2(String[] args) throws IOException {
//        avformat.AVFormatContext pFormatCtx = new avformat.AVFormatContext(null);
//        int             i, videoStream;
//        avcodec.AVCodecContext pCodecCtx = null;
//        avcodec.AVCodec pCodec = null;
//        avutil.AVFrame pFrame = null;
//        avutil.AVFrame pFrameRGB = null;
//        avcodec.AVPacket packet = new avcodec.AVPacket();
//        int[]           frameFinished = new int[1];
//        int             numBytes;
//        BytePointer buffer = null;
//
//        avutil.AVDictionary optionsDict = null;
//        swscale.SwsContext sws_ctx = null;
//
//        if (args.length < 1) {
//            System.out.println("Please provide a movie file");
//            System.exit(-1);
//        }
//        // Register all formats and codecs
//        av_register_all();
//
//        // Open video file
//        if (avformat_open_input(pFormatCtx, args[0], null, null) != 0) {
//            System.exit(-1); // Couldn't open file
//        }
//
//        // Retrieve stream information
//        if (avformat_find_stream_info(pFormatCtx, (PointerPointer)null) < 0) {
//            System.exit(-1); // Couldn't find stream information
//        }
//
//        // Dump information about file onto standard error
//        av_dump_format(pFormatCtx, 0, args[0], 0);
//
//        // Find the first video stream
//        videoStream = -1;
//        for (i = 0; i < pFormatCtx.nb_streams(); i++) {
//            if (pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO) {
//                videoStream = i;
//                break;
//            }
//        }
//        if (videoStream == -1) {
//            System.exit(-1); // Didn't find a video stream
//        }
//
//        // Get a pointer to the codec context for the video stream
//        pCodecCtx = pFormatCtx.streams(videoStream).codec();
//
//        // Find the decoder for the video stream
//        pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
//        if (pCodec == null) {
//            System.err.println("Unsupported codec!");
//            System.exit(-1); // Codec not found
//        }
//        // Open codec
//        if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
//            System.exit(-1); // Could not open codec
//        }
//
//        // Allocate video frame
//        pFrame = av_frame_alloc();
//
//        // Allocate an AVFrame structure
//        pFrameRGB = av_frame_alloc();
//        if(pFrameRGB == null) {
//            System.exit(-1);
//        }
//
//        // Determine required buffer size and allocate buffer
//        numBytes = avpicture_get_size(AV_PIX_FMT_RGB24,
//                pCodecCtx.width(), pCodecCtx.height());
//        buffer = new BytePointer(av_malloc(numBytes));
//
//        sws_ctx = sws_getContext(pCodecCtx.width(), pCodecCtx.height(),
//                pCodecCtx.pix_fmt(), pCodecCtx.width(), pCodecCtx.height(),
//                AV_PIX_FMT_RGB24, SWS_BILINEAR, null, null, (DoublePointer)null);
//
//        // Assign appropriate parts of buffer to image planes in pFrameRGB
//        // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
//        // of AVPicture
//        avpicture_fill(new avcodec.AVPicture(pFrameRGB), buffer, AV_PIX_FMT_RGB24,
//                pCodecCtx.width(), pCodecCtx.height());
//
//        // Read frames and save first five frames to disk
//        i = 0;
//        while (av_read_frame(pFormatCtx, packet) >= 0) {
//            // Is this a packet from the video stream?
//            if (packet.stream_index() == videoStream) {
//                // Decode video frame
//                avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);
//
//                // Did we get a video frame?
//                if (frameFinished[0] != 0) {
//                    // Convert the image from its native format to RGB
//                    sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0,
//                            pCodecCtx.height(), pFrameRGB.data(), pFrameRGB.linesize());
//
//                    // Save the frame to disk
//                    if (++i<=5) {
//                        SaveFrame(pFrameRGB, pCodecCtx.width(), pCodecCtx.height(), i);
//                    }
//                }
//            }
//
//            // Free the packet that was allocated by av_read_frame
//            av_free_packet(packet);
//        }
//
//        // Free the RGB image
//        av_free(buffer);
//        av_free(pFrameRGB);
//
//        // Free the YUV frame
//        av_free(pFrame);
//
//        // Close the codec
//        avcodec_close(pCodecCtx);
//
//        // Close the video file
//        avformat_close_input(pFormatCtx);
//
//        System.exit(0);
//    }
}
