package com.bc.utils;

import java.time.format.DateTimeFormatter;

/**
 * Created by mrt on 2019/4/12 0012 下午 1:26
 */
public class DateUtil {
    public static final DateTimeFormatter HOUR_MIN_ONE = DateTimeFormatter.ofPattern("H:mm");
    public static final DateTimeFormatter HOUR_MIN_TWO = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter HOUR_MIN_SEC = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter YEAR_MONTH_DAY_MORE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
