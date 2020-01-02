package com.bc.service.redPacket.temp;

import com.bc.service.common.redPacket.entity.VsTime;
import com.bc.service.common.redPacket.service.IVsTimeService;
import com.bc.service.redPacket.vo.HongBaoYuTime;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by mrt on 2020/1/1 0001 21:06
 */
@Service
public class HongBaoServer {

    private IVsTimeService timeService;

    /**
     * 1.如果没开始：下次开始的时间戳+持续时间
     */
    public HongBaoYuTime getHongBaoYuTime() {
        HongBaoYuTime hongBaoYuTime = new HongBaoYuTime();
        LocalTime localTime = LocalTime.now();
        List<VsTime> list = timeService.list();
        for (VsTime vsTime : list) {
            if (localTime.compareTo(vsTime.getEndTime()) < 0) {
                if (localTime.compareTo(vsTime.getStartTime()) > 0) {
                    hongBaoYuTime.setCountDown(0L);
                    hongBaoYuTime.setTimeLeft((long) (vsTime.getEndTime().getSecond() - localTime.getSecond()));
                    return hongBaoYuTime;
                } else {
                    hongBaoYuTime.setCountDown((long) (vsTime.getStartTime().getSecond() - localTime.getSecond()));
                    hongBaoYuTime.setTimeLeft(0L);
                    return hongBaoYuTime;
                }
            }
        }
        int sec = LocalTime.MAX.getSecond() - localTime.getSecond() + list.get(0).getStartTime().getSecond();
        hongBaoYuTime.setCountDown((long) sec);
        hongBaoYuTime.setTimeLeft(0L);
        return hongBaoYuTime;
    }
}
