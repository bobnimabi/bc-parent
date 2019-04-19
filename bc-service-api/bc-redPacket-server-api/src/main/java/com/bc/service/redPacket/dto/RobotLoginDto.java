package com.bc.service.redPacket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by mrt on 2019/4/12 0012 下午 9:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotLoginDto implements Serializable {
    private String varCode;
    private Integer num;
}
