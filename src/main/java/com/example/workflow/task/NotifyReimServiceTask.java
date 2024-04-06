package com.example.workflow.task;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.example.workflow.db.dao.TbUserMapper;
import com.example.workflow.db.pojo.TbReim;
import com.example.workflow.db.pojo.TbUser;
import com.example.workflow.service.SendEmailService;
import com.example.workflow.service.TbReimService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Slf4j
public class NotifyReimServiceTask implements JavaDelegate {

    @Resource
    private TbReimService reimService;

    @Resource
    private SendEmailService sendEmailService;

    @Resource
    private TbUserMapper tbUserMapper;

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = execution.getVariables();
        String result = (String) variables.get("result");
        String notifyUrl = MapUtil.getStr(variables, "notifyUrl");
        String title = MapUtil.getStr(variables, "title");

        Integer status = "同意".equals(result) ? 3 : 2;
        boolean updateResult = reimService.update()
                .set("status", status)
                .eq("instance_id", processInstanceId)
                .update();
        if (!updateResult) {
            log.info("{}的审批结果为：{}", title, result);
        } else {
            // 1.可以归档
            execution.setVariable("filing", true);

            // 2.向调用方发送通知，告知审批结果
            HttpResponse response = HttpRequest
                    .get(notifyUrl + "?title=" + title + "&result=" + result).execute();
            if (response.getStatus() == 200) {
                log.info("向{}发送 {} 的审批结果成功", notifyUrl, title);
            }
            // 3.向报销申请人发送邮件通知
            TbReim tbReim = reimService.query().eq("instance_id", processInstanceId).one();
            TbUser tbUser = tbUserMapper.selectById(tbReim.getUserId());
            String content = "报销人员：" + tbUser.getName() + "\n"
                    + "报销申请审批结果：" + result + "\n"
                    + "报销类型：" + (tbReim.getTypeId() == 1 ? "普通报销" : "差旅报销") + "\n"
                    + "报销金额：" + tbReim.getAmount() + " 元\n"
                    + "借款金额：" + tbReim.getAnleihen() + " 元\n"
                    + "实际金额：" + tbReim.getBalance() + " 元\n";
            sendEmailService.sendSimpleEmail(tbUser.getEmail(), "报销申请审批结果", content);
        }
    }
}
