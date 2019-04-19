package com.bc.manager.redPacket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by mrt on 2019/4/9 0009 下午 1:25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VsAwardActiveDtoList {
    private List<VsAwardPrizeDto> prizeDtoList;
}
