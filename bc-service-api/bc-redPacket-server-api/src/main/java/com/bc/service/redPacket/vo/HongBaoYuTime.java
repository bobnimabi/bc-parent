package com.bc.service.redPacket.vo;

import lombok.Data;

/**
 * Created by mrt on 2020/1/1 0001 21:09
 */
@Data
public class HongBaoYuTime {
    // 红包雨没开始：还有多少秒开始
    private Long countDown;
    // 红包雨开始：还剩几秒结束
    private Long timeLeft;
}
