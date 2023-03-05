package com.receiver.controller;


import com.receiver.service.interfaces.VideoConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping
public class VideoReceiverController {

    private final VideoConverter videoConverter;

    @Autowired
    public VideoReceiverController(VideoConverter videoConverter) {
        this.videoConverter = videoConverter;
    }

    @PostMapping("/upload")
    public void convertVideo(@RequestParam("file") MultipartFile file) throws IOException {
        videoConverter.convertVideoToFrames(file.getInputStream(), file.getOriginalFilename()+ "-" + UUID.randomUUID().toString());
    }
}
