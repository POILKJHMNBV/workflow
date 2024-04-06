package com.example.workflow.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.workflow.controller.form.*;
import com.example.workflow.service.WorkflowService;
import com.example.workflow.util.R;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/workflow")
@Slf4j
public class WorkFlowController {

    @Resource
    private WorkflowService workflowService;

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private HistoryService historyService;

    @PostMapping("/startMeetingProcess")
    public R startMeetingProcess(@Valid @RequestBody StartMeetingProcessForm startMeetingProcessForm) {
        HashMap<String, Object> param = JSONUtil.parse(startMeetingProcessForm).toBean(HashMap.class);
        if (startMeetingProcessForm.getGmId() == null) {
            param.put("identity", "总经理");
            param.put("result", "同意");
        } else {
            param.put("identity", "员工");
        }
        param.put("type", "会议申请");
        param.put("filing", false);
        param.put("createDate", DateUtil.today());
        return R.ok().put("instanceId", workflowService.startMeetingProcess(param));
    }

    @PostMapping("/startLeaveProcess")
    public R startLeaveProcess(@Valid @RequestBody StartLeaveProcessForm startLeaveProcessForm) {
        HashMap<String, Object> param = JSONUtil.parse(startLeaveProcessForm).toBean(HashMap.class);
        param.put("type", "员工请假");
        param.put("filing", false);
        param.put("createDate", DateUtil.today());
        return R.ok().put("instanceId", workflowService.startLeaveProcess(param));
    }

    @PostMapping("/startReimProcess")
    public R startReimProcess(@Valid @RequestBody StartReimProcessForm startReimProcessForm) {
        HashMap<String, Object> param = JSONUtil.parse(startReimProcessForm).toBean(HashMap.class);
        param.put("type", "报销申请");
        param.put("filing", false);
        param.put("createDate", DateUtil.today());
        return R.ok().put("instanceId", workflowService.startReimProcess(param));
    }

    @PostMapping("/deleteProcessById")
    public R deleteProcessById(@Valid @RequestBody DeleteProcessByIdForm form) {
        workflowService.deleteProcessById(form.getUuid(), form.getInstanceId(), form.getType(), form.getReason());
        return R.ok();
    }

    @PostMapping("/searchTaskByPage")
    public R searchTaskByPage(@Valid @RequestBody SearchTaskByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap<String, Object> param = JSONUtil.parse(form).toBean(HashMap.class);
        param.remove("page");
        param.put("start", start);
        HashMap<String, Object> map = workflowService.searchTaskByPage(param);
        return R.ok().put("page", map);
    }

    @PostMapping("/searchApprovalContent")
    public R searchApprovalContent(@Valid @RequestBody SearchApprovalContentForm form) {
        HashMap<String, Object> map = workflowService
                .searchApprovalContent(form.getInstanceId(),
                        form.getUserId(),
                        form.getRole(),
                        form.getType(),
                        form.getStatus());
        return R.ok().put("content", map);
    }

    @GetMapping("/searchApprovalBpmn")
    public void searchApprovalBpmn(@RequestParam("instanceId") String instanceId, HttpServletResponse response) {
        response.setContentType("image/jpg");
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        BpmnModel bpmnModel;
        List<String> activeActivityIds;
        if (task != null) {
            bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            activeActivityIds = runtimeService.getActiveActivityIds(task.getExecutionId());
        } else {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list().get(0);
            bpmnModel = repositoryService.getBpmnModel(historicTaskInstance.getProcessDefinitionId());
            activeActivityIds = new ArrayList<>();
        }
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        String font = "SimSun";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("win")) {
            font = "宋体";
        }
        InputStream in = diagramGenerator
                .generateDiagram(
                        bpmnModel,
                        "jpg",
                        activeActivityIds,
                        activeActivityIds,
                        font,
                        font,
                        font,
                        processEngine.getProcessEngineConfiguration().getProcessEngineConfiguration().getClassLoader(),
                        1.0);
        try {
            IOUtils.copy(in, response.getOutputStream());
        } catch (IOException e) {
            log.error("Generate BPMN image failure!", e);
        }
    }

    @PostMapping("/approvalTask")
    public R approvalTask(@Valid @RequestBody ApprovalTaskForm approvalTaskForm) {
        workflowService.approvalTask(approvalTaskForm.getTaskId(), approvalTaskForm.getApproval());
        return R.ok();
    }

    @PostMapping("/archiveTask")
    public R archiveTask(@Valid @RequestBody ArchiveTaskForm archiveTaskForm) {
        workflowService.archiveTask(archiveTaskForm.getTaskId(),
                archiveTaskForm.getUserId(),
                JSONUtil.parseArray(archiveTaskForm.getFiles()));
        return R.ok();
    }

    @PostMapping("/turnTask")
    public R turnTask(@Valid @RequestBody TurnTaskForm turnTaskForm) {
        Integer userId = turnTaskForm.getUserId();
        Integer assignId = turnTaskForm.getAssignId();
        if (userId.equals(assignId)) {
            return R.error("userId和assignId不能相等");
        }
        workflowService.turnTask(userId, assignId);
        return R.ok();
    }

    @GetMapping("/searchApprovalTaskCount")
    public R searchApprovalTaskCount(@RequestParam("userId") Integer userId) {
        if (userId == null || userId < 1) {
            return R.ok();
        }
        return R.ok().put("count", workflowService.searchApprovalTaskCount(userId));
    }
}
