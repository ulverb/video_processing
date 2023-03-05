package com.ir.managers;

import com.ir.kafka.dto.VideoFrame;
import com.ir.service.interfaces.ImageResizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class ImageManager {

    private static final String FRAME_EXTENSION = "png";
    private static final String SAVED_FILES_PATH = "tmp/resizedFrames";

    private final ImageResizer imageResizerService;

    public ImageManager(ImageResizer imageResizerService) {
        this.imageResizerService = imageResizerService;
    }

    public void handleFrameMessage(VideoFrame videoFrame){

        try{
            InputStream is = new ByteArrayInputStream(videoFrame.getBytes());
            BufferedImage origImage = ImageIO.read(is);

            BufferedImage resizedImage = imageResizerService.resizeImage(origImage);

            String imageName = "image-" + System.currentTimeMillis() + "." + FRAME_EXTENSION;

            Path uploadPath = Paths.get(SAVED_FILES_PATH + "/" + "video-instance-" + videoFrame.getVideoOriginalFileName());
            Path filePath = uploadPath.resolve(imageName);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            ImageIO.write(resizedImage,FRAME_EXTENSION, new File(String.valueOf(filePath)));

            //TODO storage on S3

        }catch(Exception e){

        }
    }

}
