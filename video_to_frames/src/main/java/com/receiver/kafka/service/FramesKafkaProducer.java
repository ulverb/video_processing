package com.receiver.kafka.service;

import com.receiver.kafka.dto.VideoFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FramesKafkaProducer {


    @Value("${topic.name.producer.frame}")
    private  String frameProducerTopicName;

    private KafkaTemplate<String, VideoFrame> frameKafkaTemplate;

    @Autowired
    public FramesKafkaProducer(KafkaTemplate<String, VideoFrame> frameKafkaTemplate) {
        this.frameKafkaTemplate = frameKafkaTemplate;
    }

    public void sendFrameMessage(VideoFrame frame) {
        ProducerRecord<String, VideoFrame> record = new ProducerRecord<>(frameProducerTopicName, frame);

        try {
            frameKafkaTemplate.send(record);
        }catch(Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        }
        finally {
            frameKafkaTemplate.flush();
        }
    }
}
