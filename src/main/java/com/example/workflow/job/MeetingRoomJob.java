package com.example.workflow.job;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class MeetingRoomJob extends QuartzJobBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("开始创建线上会议室！");
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String uuid = MapUtil.getStr(jobDataMap, "uuid");
        String end = MapUtil.getStr(jobDataMap, "end");
        LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Duration expire = Duration.between(LocalDateTime.now(), endTime);
        Long roomId = RandomUtil.randomLong(1L, 4294967295L);

        /*stringRedisTemplate.opsForValue().set("roomId-" + roomId, uuid);
        stringRedisTemplate.expire("roomId-" + roomId, expire);*/

        stringRedisTemplate.opsForValue().set(uuid, String.valueOf(roomId));
        stringRedisTemplate.expire(uuid, expire);
        log.info("uuid = {} 的线上会议室创建成功，会议号为：{}", uuid, roomId);
    }
}
