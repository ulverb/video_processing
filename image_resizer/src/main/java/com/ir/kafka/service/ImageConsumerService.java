package com.ir.kafka.service;


import com.ir.kafka.dto.VideoFrame;
import com.ir.managers.ImageManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j

@Service
public class ImageConsumerService {

    @Value("${topic.name.consumer.frame}")
    private String topicName;

    @Value(value="${spring.kafka.consumer.group-id}")
    private String groupId;

    private ImageManager imageManager;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ImageConsumerService(ImageManager imageManager, MeterRegistry meterRegistry) {
        this.imageManager = imageManager;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "${topic.name.consumer.frame}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "frameKafkaListenerContainerFactory",
            concurrency = "3")
    public void frameConsumer(ConsumerRecord<String, VideoFrame> record){
        long now = System.currentTimeMillis();

        imageManager.handleFrameMessage(record.value());

        meterRegistry.timer("convert", List.of(Tag.of("instance", record.value().getVideoOriginalFileName())))
                .record(() -> System.currentTimeMillis() - now);

    }
}
