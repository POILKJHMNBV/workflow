package com.example.workflow.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.workflow.db.dao.TbUserMapper;
import com.example.workflow.db.pojo.TbLeave;
import com.example.workflow.db.pojo.TbUser;
import com.example.workflow.service.SendEmailService;
import com.example.workflow.service.TbLeaveService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class NotifyLeaveServiceTask implements JavaDelegate {

    @Resource
    private TbLeaveService leaveService;

    @Resource
    private SendEmailService sendEmailService;

    @Resource
    private TbUserMapper tbUserMapper;

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = execution.getVariables();
        String notifyUrl = MapUtil.getStr(variables, "notifyUrl");
        String result = (String) variables.get("result");
        String title = MapUtil.getStr(variables, "title");
        Double days = MapUtil.getDouble(variables, "days");

        log.info("{}审批结果为：{}，请假天数为{}", processInstanceId, result, days);

        Integer status = "同意".equals(result) ? 3 : 2;
        boolean updateResult = leaveService.update()
                .set("status", status)
                .eq("instance_id", processInstanceId)
                .update();
        if (!updateResult) {
            log.error("更新 {} 记录状态失败，请假天数为{}", processInstanceId, days);
        } else {
            // 1.可以归档
            execution.setVariable("filing", true);

            // 2.向调用方发送通知，告知审批结果
            HttpResponse response = HttpRequest
                    .get(notifyUrl + "?title=" + title + "&days=" + days + "&result=" + result).execute();
            if (response.getStatus() == 200) {
                log.info("向{}发送 {} 的审批结果成功", notifyUrl, title);
            }

            // 3.向请假申请人发送邮件通知
            TbLeave tbLeave = leaveService.query().eq("instance_id", processInstanceId).one();
            TbUser tbUser = tbUserMapper.selectById(tbLeave.getUserId());
            String content = "请假人员：" + tbUser.getName() + "\n"
                    + "请假申请审批结果：" + result + "\n"
                    + "请假开始时间：" + DateUtil.format(tbLeave.getStart(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                    "请假结束时间：" + DateUtil.format(tbLeave.getEnd(), "yyyy-MM-dd HH:mm:ss") + "\n";
            sendEmailService.sendSimpleEmail(tbUser.getEmail(), "请假申请审批结果", content);
        }
    }
}