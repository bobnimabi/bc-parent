package com.bc.service.login.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mrt on 2019/4/11 0011 下午 3:50
 */
@Data
public class IdListLongDto implements Serializable {
    private List<Long> ids;
}
