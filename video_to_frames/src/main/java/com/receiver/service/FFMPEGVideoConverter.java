package com.receiver.service;


import com.receiver.kafka.dto.VideoFrame;
import com.receiver.kafka.service.FramesKafkaProducer;
import com.receiver.service.interfaces.VideoConverter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


@Slf4j
@Service("FFMPEGVideoConverter")
public class FFMPEGVideoConverter implements VideoConverter {

    private static final String FRAME_EXTENSION = "png";

    private final FramesKafkaProducer framesKafkaProducer;
    private final Java2DFrameConverter converter;
    private final ExecutorService executorService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public FFMPEGVideoConverter(FramesKafkaProducer framesKafkaProducer, MeterRegistry meterRegistry) {
        this.framesKafkaProducer = framesKafkaProducer;
        this.meterRegistry = meterRegistry;
        this.converter = new Java2DFrameConverter();
        this.executorService = Executors.newFixedThreadPool(10); // todo: should be defined in application.yaml
    }

    @Override
    public void convertVideoToFrames(InputStream videoFile, String originalVideoName) {
         log.info("Start converting video");
         try{
             long now = System.currentTimeMillis();
             Java2DFrameConverter converter = new Java2DFrameConverter();
             FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile);
             frameGrabber.start();
             Frame frame;
             double frameRate=frameGrabber.getFrameRate();
             log.info("Video has "+frameGrabber.getLengthInFrames()+" frames and has frame rate of "+frameRate);

             for(int index=1;index<frameGrabber.getLengthInFrames();index++){
                 frameGrabber.setFrameNumber(index);
                 frame = frameGrabber.grab();
                 BufferedImage bi = converter.convert(frame);

                 String frameName = "video-frame-" + System.currentTimeMillis() + "." + FRAME_EXTENSION;

                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ImageIO.write(bi, FRAME_EXTENSION, baos);

                 framesKafkaProducer.sendFrameMessage(VideoFrame.builder()
                         .bytes(baos.toByteArray())
                         .frameName(frameName)
                         .videoOriginalFileName(originalVideoName)
                         .index(index)
                         .numberOfFramesInVideo(frameGrabber.getLengthInFrames())
                         .build());

                 meterRegistry.timer("frames", List.of(Tag.of("instance", originalVideoName)))
                         .record(() -> System.currentTimeMillis() - now);
             }

             frameGrabber.stop();

         }catch (Exception e){
             e.printStackTrace();
         }

         log.info("Finished converting video");
     }


    @Override
    public void convertVideoToFramesWithThreads(InputStream videoFile, String originalVideoName) {
        log.info("Start converting video");
        try(FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile);) {

            frameGrabber.start();
            double frameRate = frameGrabber.getFrameRate();
            log.info("Video has " + frameGrabber.getLengthInFrames() + " frames and has frame rate of " + frameRate);
            IntStream.range(0, frameGrabber.getLengthInFrames())
                .forEach(index ->
                    executorService.submit(() -> processSingleFrame(originalVideoName, frameGrabber, index, frameGrabber.getLengthInFrames()))
                );
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Finish converting video");
    }

    private void processSingleFrame(String originalVideoName, FFmpegFrameGrabber frameGrabber, int frameIndex, int numOfFrames) {
        try {
            long now = System.currentTimeMillis();

            frameGrabber.setFrameNumber(frameIndex);
            Frame frame = frameGrabber.grab();
            BufferedImage bi = converter.convert(frame);

            String frameName = "frame-from-" + originalVideoName + "-" + System.currentTimeMillis() + "." + FRAME_EXTENSION;


            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(bi, FRAME_EXTENSION, baos);
                framesKafkaProducer.sendFrameMessage(VideoFrame.builder()
                        .bytes(baos.toByteArray())
                        .frameName(frameName)
                        .videoOriginalFileName(originalVideoName)
                        .index(frameIndex)
                        .numberOfFramesInVideo(numOfFrames)
                    .build());

                meterRegistry.timer("frames", List.of(Tag.of("instance", originalVideoName)))
                        .record(() -> System.currentTimeMillis() - now);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
