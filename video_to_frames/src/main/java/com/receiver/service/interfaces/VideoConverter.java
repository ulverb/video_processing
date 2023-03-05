package com.receiver.service.interfaces;

import java.io.InputStream;

public interface VideoConverter {

    void convertVideoToFrames(InputStream videoFile, String originalFileName);
    void convertVideoToFramesWithThreads(InputStream videoFile, String originalFileName);
}
