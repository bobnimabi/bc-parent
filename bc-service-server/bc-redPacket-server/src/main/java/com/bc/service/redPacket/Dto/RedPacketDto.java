package com.bc.service.redPacket.Dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by mrt on 2019/4/12 0012 下午 12:47
 */
@Data
public class RedPacketDto implements Serializable {
    private String username;
    private String clientType;
    private String clientIp;
}
