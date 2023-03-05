package com.app.test;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestApp {

    public static String RECEIVER_URL = "http://localhost:8001/upload";

    public static void main(String... args) throws InterruptedException {
        if (args == null || args.length != 3)
            return;


        Path filePath = Paths.get(args[0]);
        int instances = Integer.valueOf(args[1]);
        int seconds = Integer.valueOf(args[2]);

        AtomicInteger ai = new AtomicInteger(0);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(instances);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (ai.incrementAndGet() <= instances) {
                sendHttp(ai.get()+ "-" + filePath.getFileName().toString(), filePath.toFile());
            } else {
                scheduledExecutorService.shutdown();
            }
        }, 0, seconds, TimeUnit.SECONDS);

        scheduledExecutorService.awaitTermination(60, TimeUnit.MINUTES);
    }

    private static void sendHttp(String fileName, File file) {
        HttpPost post = new HttpPost(RECEIVER_URL);
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);
        builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, fileName);
        final HttpEntity entity = builder.build();

        post.setEntity(entity);
        post.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(100, TimeUnit.MILLISECONDS)
                        .setConnectTimeout(100, TimeUnit.MILLISECONDS)
                .build());
        try(CloseableHttpClient client = HttpClientBuilder.create()
                .build();
            CloseableHttpResponse response = client
                    .execute(post, classicHttpResponse -> null)) {
            // nothing
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}