package com.example.workflow.service;

import com.example.workflow.db.pojo.TbMeeting;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* @author zhenwu
* @description 针对表【tb_meeting(会议表)】的数据库操作Service
* @createDate 2023-06-04 21:05:52
*/
public interface TbMeetingService extends IService<TbMeeting> {
    /**
     * 根据工作流实例id查询会议详情信息
     * @param instanceId 工作流实例id
     * @return 会议详情信息
     */
    HashMap<String, Object> searchMeetingByInstanceId(String instanceId);
}
