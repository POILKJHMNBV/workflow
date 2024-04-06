package com.example.workflow.service.impl;

import com.example.workflow.service.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class QuartzServiceImpl implements QuartzService {

    @Resource
    private Scheduler scheduler;

    @Override
    public void addJob(JobDetail jobDetail, String jobName, String jobGroupName, Date start) {
        try {
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName, jobGroupName)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .startAt(start).build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("成功添加" + jobGroupName + "-" + jobName + "定时器！");
        } catch (SchedulerException e) {
            log.error("定时器" + jobGroupName + "-" + jobName + "添加失败！", e);
        }
    }

    @Override
    public void deleteJob(String jobName, String jobGroupName) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        try {
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
            log.info("成功删除" + jobGroupName + "-" + jobName + "定时器！");
        } catch (SchedulerException e) {
            log.error("定时器" + jobGroupName + "-" + jobName + "删除失败！", e);
        }
    }
}
