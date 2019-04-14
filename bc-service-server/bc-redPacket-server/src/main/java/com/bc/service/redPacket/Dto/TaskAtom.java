package com.bc.service.redPacket.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by mrt on 2019/4/12 0012 下午 9:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAtom implements Serializable {
    private Integer userId;
    private String username;
    private Long recordId;
}
