package ru.apelsina8.fifascouter.rtmp;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class RtmpReader {

    private Java2DFrameConverter converter;
    private FFmpegFrameGrabber grabber;
    private String url;

    public RtmpReader() {
        this.converter = new Java2DFrameConverter();
    }

    // Метод для установки RTMP URL
    public void setUrl(String url) throws FFmpegFrameGrabber.Exception {
        if (grabber != null) {
            grabber.stop();
            grabber.release();
        }

        this.url = url;
        this.grabber = new FFmpegFrameGrabber(url);

        // Установка дополнительных параметров для RTMP
        grabber.setOption("rtmp_transport", "tcp"); // Использование TCP для транспорта RTMP
        grabber.setFormat("flv");
        grabber.setOption("rtmp_buffer", "1000");
        grabber.setOption("analyzeduration", "1000000");
        grabber.start();
    }

    public String getUrl() {
        return url;
    }

    public BufferedImage getFrameImage() {
        try {
            return converter.convert(grabber.grabImage());
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException("Failed to grab image frame", e);
        }
    }

    public void stop() {
        try {
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException("Failed to stop grabber", e);
        }
    }
}


