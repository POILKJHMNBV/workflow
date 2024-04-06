package com.example.workflow.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.workflow.db.pojo.TbAmect;
import com.example.workflow.db.pojo.TbAmectType;
import com.example.workflow.db.pojo.TbMeeting;
import com.example.workflow.service.TbAmectService;
import com.example.workflow.service.TbAmectTypeService;
import com.example.workflow.service.TbMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Component
@Slf4j
public class MeetingStatusJob extends QuartzJobBean {

    @Resource
    private TbMeetingService meetingService;

    @Resource
    private TbAmectService tbAmectService;

    @Resource
    private TbAmectTypeService tbAmectTypeService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String uuid = MapUtil.getStr(jobDataMap, "uuid");
        String status = MapUtil.getStr(jobDataMap, "status");
        String date = MapUtil.getStr(jobDataMap, "date");
        String start = MapUtil.getStr(jobDataMap, "start");
        String end = MapUtil.getStr(jobDataMap, "end");
        String flag = MapUtil.getStr(jobDataMap, "flag");

        // 更新会议状态
        meetingService.update().eq("uuid", uuid).set("status", status).update();
        log.info("会议{}状态更新成功！", uuid);

        if ("end".equals(flag)) {
            // 统计线上会议缺席人员，并生成罚款单
            TbMeeting tbMeeting = meetingService.query()
                    .select("title", "members", "present", "type")
                    .eq("uuid", uuid).one();
            if (tbMeeting.getType() == 1) {
                JSONArray members = JSONUtil.parseArray(tbMeeting.getMembers());
                JSONArray present = JSONUtil.parseArray(tbMeeting.getPresent());
                members.removeAll(present);
                boolean updateResult = meetingService.update()
                        .set("unpresent", members.toString())
                        .eq("uuid", uuid).update();
                if (updateResult) {
                    TbAmectType amectType = tbAmectTypeService.query()
                            .select("id", "money")
                            .eq("type", "缺席会议")
                            .one();
                    String reason = "缺席"+ date
                            + " " + start + "~" + end
                            + "的" + tbMeeting.getTitle();
                    for (Object member : members) {
                        TbAmect tbAmect = new TbAmect();
                        tbAmect.setUserId((Integer) member);
                        tbAmect.setAmount(amectType.getMoney());
                        tbAmect.setTypeId(amectType.getId());
                        tbAmect.setReason(reason);
                        tbAmect.setUuid(IdUtil.simpleUUID());
                        tbAmect.setStatus(1);
                        tbAmectService.save(tbAmect);
                    }
                }
            }
        }
    }
}
