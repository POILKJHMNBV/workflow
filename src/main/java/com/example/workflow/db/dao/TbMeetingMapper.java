package com.example.workflow.db.dao;

import com.example.workflow.db.pojo.TbMeeting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_meeting(会议表)】的数据库操作Mapper
* @createDate 2023-06-04 21:05:52
* @Entity com.example.workflow.db.pojo.TbMeeting
*/
public interface TbMeetingMapper extends BaseMapper<TbMeeting> {

    /**
     * 根据工作流实例id查询会议详情信息
     * @param instanceId 工作流实例id
     * @return 会议详情信息
     */
    HashMap<String, Object> searchMeetingByInstanceId(String instanceId);
}




