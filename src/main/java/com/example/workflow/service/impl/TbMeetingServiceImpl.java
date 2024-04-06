package com.example.workflow.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.pojo.TbMeeting;
import com.example.workflow.service.TbMeetingService;
import com.example.workflow.db.dao.TbMeetingMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_meeting(会议表)】的数据库操作Service实现
* @createDate 2023-06-04 21:05:52
*/
@Service
public class TbMeetingServiceImpl extends ServiceImpl<TbMeetingMapper, TbMeeting>
    implements TbMeetingService {

    @Resource
    private TbMeetingMapper tbMeetingMapper;

    @Override
    public HashMap<String, Object> searchMeetingByInstanceId(String instanceId) {
        HashMap<String, Object> result = tbMeetingMapper.searchMeetingByInstanceId(instanceId);
        String date = result.get("date").toString();
        String start = result.get("start").toString();
        String end = result.get("end").toString();
        DateTime startTime = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm");
        DateTime endTime = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
        long hours = DateUtil.between(endTime, startTime, DateUnit.HOUR, true);
        result.put("hours", hours);
        return result;
    }
}




