package com.example.workflow.job;

import com.example.workflow.service.TbMeetingService;
import com.example.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 检查工作流的会议审批任务
 */
@Slf4j
@Component
public class MeetingWorkflowJob extends QuartzJobBean {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TbMeetingService meetingService;

    @Resource
    private WorkflowService workflowService;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        Map map = ctx.getJobDetail().getJobDataMap();
        String uuid = map.get("uuid").toString();
        String instanceId = map.get("instanceId").toString();

        // 若到了会议开始时间，工作流还未结束，则证明会议未审批完成，则删除工作流实例，更新会议状态为未审批
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (instance != null) {
            workflowService.deleteProcessById(uuid, instanceId, "会议申请", "会议过期");
            meetingService.update().eq("uuid", uuid).set("status", 6).update();
            log.info("会议{}已失效", uuid);
        }
    }
}