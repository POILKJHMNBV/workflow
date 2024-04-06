package com.example.workflow.service;

import org.quartz.JobDetail;

import java.util.Date;

public interface QuartzService {
    void addJob(JobDetail jobDetail, String jobName, String jobGroupName, Date start);
    void deleteJob(String jobName, String jobGroupName);
}
