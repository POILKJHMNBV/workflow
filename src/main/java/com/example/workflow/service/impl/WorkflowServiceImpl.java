package com.example.workflow.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.example.workflow.bo.Approval;
import com.example.workflow.job.MeetingWorkflowJob;
import com.example.workflow.service.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.engine.task.TaskInfoQuery;
import org.activiti.engine.task.TaskQuery;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Resource
    private TbMeetingService meetingService;

    @Resource
    private TbLeaveService leaveService;

    @Resource
    private TbReimService tbReimService;

    @Resource
    private QuartzService quartzService;

    @Override
    public String startMeetingProcess(HashMap<String, Object> param) {
        String instanceId = runtimeService.startProcessInstanceByKey("meeting", param).getProcessInstanceId();
        String uuid = (String) param.get("uuid");
        String date = (String) param.get("date");
        String start = (String) param.get("start");

        JobDetail jobDetail = JobBuilder.newJob(MeetingWorkflowJob.class).build();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("uuid", uuid);
        jobDataMap.put("instanceId", instanceId);
        Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm:ss");
        quartzService.addJob(jobDetail, uuid, "会议工作流组", executeDate);
        return instanceId;
    }

    @Override
    public String startLeaveProcess(HashMap<String, Object> param) {
        return runtimeService.startProcessInstanceByKey("leave", param).getProcessInstanceId();
    }

    @Override
    public String startReimProcess(HashMap<String, Object> param) {
        return runtimeService.startProcessInstanceByKey("reim", param).getProcessInstanceId();
    }

    @Override
    public void approvalTask(String taskId, String approval) {
        taskService.setVariableLocal(taskId, "result", approval);
        taskService.complete(taskId);
    }

    @Override
    public void archiveTask(String taskId, Integer userId, JSONArray files) {
        taskService.setVariable(taskId, "files", files);
        taskService.setVariable(taskId, "filing", false);
        taskService.setOwner(taskId, userId + "");
        taskService.setAssignee(taskId, userId + "");
        taskService.complete(taskId);
    }

    @Override
    public void deleteProcessById(String uuid, String instanceId, String type, String reason) {
        long count = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            runtimeService.deleteProcessInstance(instanceId, reason);
        }
        count = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            historyService.deleteHistoricProcessInstance(instanceId);
        }
        if (type.equals("会议申请")) {
            quartzService.deleteJob(uuid, "会议开始任务组");
            quartzService.deleteJob(uuid, "会议结束任务组");
            quartzService.deleteJob(uuid, "会议工作流组");
            quartzService.deleteJob(uuid, "创建线上会议室ID任务组");
        }
    }

    @Override
    public HashMap<String, Object> searchTaskByPage(HashMap<String, Object> param) {
        ArrayList<Approval> approvals = null;
        int userId = (Integer) param.get("userId");
        JSONArray role = (JSONArray) param.get("role");
        int start = (int) param.get("start");
        int length = (int) param.get("length");
        String type = (String) param.get("type");
        String status = (String) param.get("status");
        String creatorName = (String) param.get("creatorName");
        String instanceId = (String) param.get("instanceId");

        long totalCount = 0L;
        ArrayList<String> assigneeList = new ArrayList<>(role.size());
        assigneeList.add(userId + "");
        role.forEach(one -> assigneeList.add(one.toString()));

        if ("待审批".equals(status)) {
            TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                    .includeProcessVariables().includeTaskLocalVariables().taskAssigneeIds(assigneeList);
            paramIsNotBlank(taskQuery, creatorName, type, instanceId);
            totalCount = taskQuery.count();
            List<Task> taskList = taskQuery.listPage(start, length);
            approvals = createApprovals(taskList, status);
        } else if ("已审批".equals(status)) {
            HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                    .orderByHistoricTaskInstanceStartTime().desc()
                    .includeTaskLocalVariables().includeProcessVariables()
                    .taskAssigneeIds(assigneeList).finished().processUnfinished();
            paramIsNotBlank(historicTaskInstanceQuery, creatorName, type, instanceId);
            totalCount = historicTaskInstanceQuery.count();
            List<HistoricTaskInstance> historicTaskInstanceList = historicTaskInstanceQuery.listPage(start, length);
            approvals = createApprovals(historicTaskInstanceList, status);
        } else if ("已结束".equals(status)) {
            HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                    .orderByHistoricTaskInstanceStartTime().desc()
                    .includeTaskLocalVariables().includeProcessVariables()
                    .taskAssigneeIds(assigneeList).finished().processFinished();
            paramIsNotBlank(historicTaskInstanceQuery, creatorName, type, instanceId);
            totalCount = historicTaskInstanceQuery.count();
            List<HistoricTaskInstance> historicTaskInstanceList = historicTaskInstanceQuery.listPage(start, length);
            approvals = createApprovals(historicTaskInstanceList, status);
        }
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("list", approvals);
        map.put("totalCount", totalCount);
        map.put("pageIndex", start);
        map.put("pageSize", length);
        return map;
    }

    @Override
    public HashMap<String, Object> searchApprovalContent(String instanceId, int userId, String[] role, String type, String status) {
        HashMap<String, Object> result = null;
        ArrayList<String> assigneeList = new ArrayList<>(role.length);
        assigneeList.add(userId + "");
        assigneeList.addAll(Arrays.asList(role));
        if ("会议申请".equals(type)) {
            result = meetingService.searchMeetingByInstanceId(instanceId);
        } else if ("员工请假".equals(type)) {
            result = leaveService.searchLeaveByInstanceId(instanceId);
        } else if ("报销申请".equals(type)) {
            result = tbReimService.searchReimByInstanceId(instanceId);
        }

        Map<String, Object> variables;
        if (!"已结束".equals(status)) {
            variables = runtimeService.getVariables(instanceId);
        } else {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables()
                    .includeProcessVariables()
                    .processInstanceId(instanceId)
                    .taskAssigneeIds(assigneeList)
                    .processFinished().list().get(0);
            variables = historicTaskInstance.getProcessVariables();
        }
        if (variables != null && variables.containsKey("files") && result != null) {
            ArrayNode files = (ArrayNode) variables.get("files");
            result.put("files", files);
        }

        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();

        if (result != null) {
            if (instance != null) {
                result.put("result", "");
            } else {
                if (variables != null) {
                    result.put("result", variables.get("result"));
                }
            }
        }
        return result;
    }

    @Override
    public void turnTask(int userId, int assignId) {
        taskService.createTaskQuery().taskAssignee(userId + "").list().forEach(task -> {
            String taskId = task.getId();
            taskService.setOwner(taskId, assignId + "");
            taskService.setAssignee(taskId, assignId + "");
        });
    }

    @Override
    public long searchApprovalTaskCount(int userId) {
        TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                .includeProcessVariables().includeTaskLocalVariables().taskAssignee(userId + "");
        return taskQuery.count();
    }

    private void paramIsNotBlank(TaskInfoQuery taskInfoQuery, String creatorName, String type, String instanceId) {
        if (StrUtil.isNotBlank(creatorName)) {
            taskInfoQuery.processVariableValueEquals("creatorName", creatorName);
        }
        if (StrUtil.isNotBlank(type)) {
            taskInfoQuery.processVariableValueEquals("type", type);
        }
        if (StrUtil.isNotBlank(instanceId)) {
            taskInfoQuery.processInstanceId(instanceId);
        }
    }

    private Approval createApproval(String processId, String status, Map<String, Object> processVariables) {
        String type = (String) processVariables.get("type");
        String createDate = (String) processVariables.get("createDate");
        Boolean filing = (Boolean) processVariables.get("filing");

        Approval approval = new Approval();
        approval.setCreatorName(processVariables.get("creatorName").toString());
        approval.setProcessId(processId);
        approval.setType(type);
        approval.setTitle(processVariables.get("title").toString());
        approval.setStatus(status);
        approval.setCreateDate(createDate);
        approval.setFiling(filing);
        approval.setResult(MapUtil.getStr(processVariables, "result"));
        return approval;
    }

    private <T extends TaskInfo> ArrayList<Approval> createApprovals(List<T> taskList, String status) {
        ArrayList<Approval> approvals = new ArrayList<>(taskList.size());
        for (T t : taskList) {
            Map<String, Object> processVariables = t.getProcessVariables();
            Approval approval = createApproval(t.getProcessInstanceId(), status, processVariables);
            approval.setTaskId(t.getId());
            approvals.add(approval);
        }
        return approvals;
    }
}
