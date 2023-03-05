package com.ir.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
public class VideoFrame implements Serializable {
    private String frameName;
    private String started;
    private int index;
    private String videoOriginalFileName;
    private Integer numberOfFramesInVideo;
    private byte[] bytes;


}
