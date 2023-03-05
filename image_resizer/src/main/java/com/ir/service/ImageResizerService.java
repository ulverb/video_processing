package com.ir.service;


import com.ir.service.interfaces.ImageResizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@Service
public class ImageResizerService implements ImageResizer {

    public static int TARGET_WIDTH = 70;
    public static int TARGET_HEIGHT = 70;
    @Override
    public BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
