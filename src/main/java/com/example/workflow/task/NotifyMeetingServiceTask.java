package com.example.workflow.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.workflow.db.dao.TbUserMapper;
import com.example.workflow.db.pojo.TbMeeting;
import com.example.workflow.db.pojo.TbUser;
import com.example.workflow.job.MeetingRoomJob;
import com.example.workflow.job.MeetingStatusJob;
import com.example.workflow.service.QuartzService;
import com.example.workflow.service.SendEmailService;
import com.example.workflow.service.TbMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotifyMeetingServiceTask implements JavaDelegate {

    @Resource
    private QuartzService quartzService;

    @Resource
    private TbMeetingService meetingService;

    @Resource
    private SendEmailService sendEmailService;

    @Resource
    private TbUserMapper tbUserMapper;

    @Override
    public void execute(DelegateExecution execution) {
        Map<String, Object> variables = execution.getVariables();
        String uuid = MapUtil.getStr(variables, "uuid");
        String notifyUrl = MapUtil.getStr(variables, "notifyUrl");
        String result = MapUtil.getStr(variables, "result");

        log.info("会议{}审批结果为:{}", uuid, result);

        boolean updateResult;
        TbMeeting meeting = meetingService
                .query()
                .select("id", "title", "creator_id", "date", "place", "type", "start", "end")
                .eq("uuid", uuid).one();
        if ("同意".equals(result)) {
            String date = meeting.getDate();
            String start = meeting.getStart();
            String end = meeting.getEnd();

            // 将会议状态改为审批通过
            updateResult = meetingService.update().eq("uuid", uuid).set("status", 3).update();
            if (updateResult) {
                String meetingType = MapUtil.getStr(variables, "meetingType");
                if ("线上会议".equals(meetingType)) {
                    // 由于线上会议申请的时候没有会议室，因此需要在会议开始前15分钟生成会议号存入redis中
                    JobDetail jobDetail = JobBuilder.newJob(MeetingRoomJob.class).build();
                    JobDataMap jobDataMap = jobDetail.getJobDataMap();
                    jobDataMap.put("uuid", uuid);
                    jobDataMap.put("end", date + " " + end);
                    Date executeTime = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm").offset(DateField.MINUTE, -15);
                    quartzService.addJob(jobDetail, uuid, "创建线上会议室ID任务组", executeTime);
                }
                quartzService.deleteJob(uuid, "会议工作流组");

                // 添加定时器，到会议开始时，将会议状态改为 4-会议进行中
                JobDetail meetingStartJobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
                JobDataMap meetingStartJobDetailJobDataMap = meetingStartJobDetail.getJobDataMap();
                meetingStartJobDetailJobDataMap.put("uuid", uuid);
                meetingStartJobDetailJobDataMap.put("status", 4);
                meetingStartJobDetailJobDataMap.put("flag", "start");
                meetingStartJobDetailJobDataMap.put("date", date);
                meetingStartJobDetailJobDataMap.put("start", start);
                meetingStartJobDetailJobDataMap.put("end", end);
                Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm");
                quartzService.addJob(meetingStartJobDetail, uuid, "会议开始任务组", executeDate);

                // 添加定时器，到会议结束时，将会议状态改为 5-会议结束，并统计会议的缺席人员
                JobDetail meetingEndJobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
                JobDataMap meetingEndJobDetailJobDataMap = meetingEndJobDetail.getJobDataMap();
                meetingEndJobDetailJobDataMap.put("uuid", uuid);
                meetingEndJobDetailJobDataMap.put("status", 5);
                meetingEndJobDetailJobDataMap.put("flag", "end");
                meetingEndJobDetailJobDataMap.put("date", date);
                meetingEndJobDetailJobDataMap.put("start", start);
                meetingEndJobDetailJobDataMap.put("end", end);
                executeDate = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
                quartzService.addJob(meetingEndJobDetail, uuid, "会议结束任务组", executeDate);
            }
        } else {
            // 将会议状态改为审批未通过
            updateResult = meetingService.update().eq("uuid", uuid).set("status", 2).update();
            quartzService.deleteJob(uuid, "会议工作流组");
        }

        if (updateResult) {
            // 向调用方发送通知，告知审批结果
            HttpResponse response = HttpRequest.get(notifyUrl + "?uuid=" + uuid + "&result=" + result).execute();
            if (response.getStatus() == 200) {
                log.info("向{}发送会议{}的审批结果成功", notifyUrl, uuid);
            }

            // 向会议申请者和会议参会人员发送邮件通知
            Long creatorId = meeting.getCreatorId();
            TbUser meetingCreator = tbUserMapper.selectById(creatorId);
            String creatorEmail = meetingCreator.getEmail();
            String meetingStartTime = meeting.getDate() + " " + meeting.getStart();
            String meetingEndTime = meeting.getDate() + " " + meeting.getEnd();
            StringBuilder content = new StringBuilder("会议主题：" + meeting.getTitle() + "\n");
            if (meeting.getType() == 1) {
                content.append("会议类型：线上会议" + "\n");
            } else {
                content.append("会议类型：线下会议" + "\n")
                        .append("会议地点：").append(meeting.getPlace()).append("\n");
            }
            content.append("会议开始时间：").append(meetingStartTime).append("\n")
                    .append("会议结束时间：").append(meetingEndTime).append("\n");
            sendEmailService.sendSimpleEmail(creatorEmail,
                    "会议申请审批结果",
                    "会议申请人员：" + meetingCreator.getName() + "\n" + "会议申请审批结果：" + result + "\n" + content);
            if ("同意".equals(result)) {
                List<String> meetingMemberEmailList = tbUserMapper.queryEmailMeetingMembers(meeting.getId());
                for (String email : meetingMemberEmailList) {
                    sendEmailService.sendSimpleEmail(email, "参会通知", content.toString());
                }
            }
        } else {
            log.error("更新会议{}的状态失败", uuid);
        }
    }
}
