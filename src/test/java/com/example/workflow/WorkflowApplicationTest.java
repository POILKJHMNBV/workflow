package com.example.workflow;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.workflow.db.pojo.TbMeeting;
import com.example.workflow.service.TbMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
@Slf4j
class WorkflowApplicationTest {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Resource
    private TbMeetingService meetingService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testDemo01() {
       runtimeService.startProcessInstanceByKey("demo_01").getProcessInstanceId();
    }

    @Test
    void testDemo02_1() {
        // 设置工作流参数
    }

    @Test
    void testDemo02_2() {
        // 以9527的身份查找与自己有关的工作流任务
        List<Task> list = taskService.createTaskQuery()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .taskAssignee("9527").
                orderByTaskCreateTime().desc().list();
        list.forEach(task -> {
            if (task.getName().equals("员工申请")) {
                taskService.complete(task.getId());
                log.info("员工任务执行完毕");
            }
        });
    }

    @Test
    void testDemo02_3() {
        // 以9528的身份查找与自己有关的工作流任务
        List<Task> list = taskService.createTaskQuery()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .taskAssignee("9528").
                orderByTaskCreateTime().desc().list();
        list.forEach(task -> {
            if (task.getName().equals("经理审批")) {
                Map<String, Object> processVariables = task.getProcessVariables();
                processVariables.put("result", "同意");
                taskService.setVariables(task.getId(), processVariables);
                taskService.complete(task.getId());
                log.info("经理任务执行完毕");
            }
        });
    }

    @Test
    void testDemo02_4() {
        // 以9527的身份查找与自己有关的已经执行完的工作流
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .includeTaskLocalVariables()
                .includeProcessVariables()
                .taskAssignee("9527")
                .processFinished()
                .orderByTaskCreateTime().desc().list();
        list.forEach(historicTaskInstance -> {
            Object result = historicTaskInstance.getProcessVariables().get("result");
            log.info("审批结果：{}", result);
        });
    }

    @Test
    void testSearchTaskByPage() {
        TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                .includeProcessVariables().includeTaskLocalVariables().taskAssignee("21");
        List<Task> list = taskQuery.list();
        for (Task task : list) {
            log.info("工作流id：{}", task.getProcessInstanceId());
        }
        log.info("待审批任务数：{}", taskQuery.count());
    }

    @Test
    void test() {
        System.out.println(DateUtil.date().offset(DateField.MINUTE, -15));
        System.out.println(stringRedisTemplate);
    }

    @Test
    void testUnPresent() {
        String uuid = "09ffa1f85a554ba1b7d52f26e5e31115";
        TbMeeting tbMeeting = meetingService.query()
                .select("id", "title", "creator_id", "date", "place", "type", "start", "end")
                .eq("uuid", uuid).one();
        System.out.println(tbMeeting);
        System.out.println("date = " + tbMeeting.getDate());
        String members = tbMeeting.getMembers();
        String present = tbMeeting.getPresent();
        JSONArray jsonArray = JSONUtil.parseArray(members);
        JSONArray parseArray = JSONUtil.parseArray(present);
        jsonArray.removeAll(parseArray);
        System.out.println("absent = " + jsonArray);
    }
}
