package com.bc.service.redPacket.temp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bc.service.common.redPacket.entity.VsAwardPlayer;
import com.bc.service.common.redPacket.entity.VsConfigure;
import com.bc.service.common.redPacket.entity.VsPayRecord;
import com.bc.service.common.redPacket.entity.VsTime;
import com.bc.service.common.redPacket.service.IVsAwardPlayerService;
import com.bc.service.common.redPacket.service.IVsConfigureService;
import com.bc.service.common.redPacket.service.IVsPayRecordService;
import com.bc.service.common.redPacket.service.IVsTimeService;
import com.bc.service.redPacket.vo.HongBaoYuTime;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mrt on 2020/1/1 0001 21:06
 */
@Slf4j
@Service
public class HongBaoServer {
    @Autowired
    private IVsTimeService timeService;
    @Autowired
    private IVsPayRecordService recordService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private IVsAwardPlayerService playerService;
    @Autowired
    private IVsConfigureService configureService;

    /**
     * 1.红包雨没开始
     */
    public HongBaoYuTime getHongBaoYuTime() {
        LocalTime localTime = LocalTime.now();
        List<VsTime> list = timeService.list();
        sort(list);
        for (VsTime vsTime : list) {
            if (localTime.compareTo(vsTime.getEndTime()) < 0) {
                if (localTime.compareTo(vsTime.getStartTime()) > 0) {
                    return createHongBaoVO(0L,
                            ChronoUnit.SECONDS.between(localTime, vsTime.getEndTime()));
                } else {
                    return createHongBaoVO(ChronoUnit.SECONDS.between(localTime, vsTime.getStartTime()),
                            ChronoUnit.SECONDS.between(vsTime.getStartTime(), vsTime.getEndTime()));
                }
            }
        }
        return getFirst(localTime,list);
    }

    private void sort(List<VsTime> list) {
        Collections.sort(list, new Comparator<VsTime>() {
            @Override
            public int compare(VsTime o1, VsTime o2) {
                return o1.getEndTime().compareTo(o2.getEndTime());
            }
        });
    }

    private HongBaoYuTime createHongBaoVO(long countDown,long timeLeft) {
        return new HongBaoYuTime(countDown,timeLeft);
    }

    private HongBaoYuTime getFirst(LocalTime localTime, List<VsTime> list) {
        VsTime vsTime = list.get(0);
        long sec = ChronoUnit.SECONDS.between(localTime, LocalTime.MAX) + ChronoUnit.SECONDS.between(LocalTime.MIN, vsTime.getStartTime());
        return createHongBaoVO(sec, ChronoUnit.SECONDS.between(vsTime.getStartTime(), vsTime.getEndTime()));
    }


    public List<VsPayRecord> listRecord() {
        IPage<VsPayRecord> page = recordService.page(new Page<VsPayRecord>(1, 50));
        page.getRecords().forEach(o -> o.setTotalAmount(MoneyUtil.convertToYuan(o.getTotalAmount())));
        return page.getRecords();
    }

    @PostConstruct
    public void time() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        List<VsTime> list = timeService.list();
                        sort(list);
                        VsTime nextStart = findNextStart();
                        LocalTime localTime = LocalTime.now();
                        LocalDateTime localDateTime = LocalDateTime.now();
                        long betweenSec = ChronoUnit.SECONDS.between(localTime, nextStart.getStartTime());
                        if (betweenSec >= 0 && betweenSec < 60) {
                            Boolean aBoolean = redis.opsForValue().setIfAbsent("redP:update_times:" +dtf.format(localDateTime)+":"+ convert(nextStart.getStartTime()), "");
                            if (aBoolean) {
                                VsConfigure configure = configureService.getOne(new QueryWrapper<VsConfigure>().eq("configure_key", "play_times"));
                                int num = Integer.parseInt(configure.getConfigureValue());
                                boolean update = playerService.update(
                                        new UpdateWrapper<VsAwardPlayer>()
                                                .eq("player_status", 1)
                                                .set("join_times", num));
                                log.info("批量更新会员参与次数：{}", update);
                            }
                        }
                    } catch (Exception e) {
                        log.info("批量更新会员参与次数异常", e);
                    } finally {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private VsTime findNextStart() {
        LocalTime localTime = LocalTime.now();
        List<VsTime> list = timeService.list();
        sort(list);
        for (VsTime vsTime : list) {
            if (localTime.compareTo(vsTime.getStartTime()) < 0) {
                return vsTime;
            }
        }
        return list.get(0);
    }

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String convert(LocalTime localTime) {
        return localTime.format(dateTimeFormatter);
    }
}
