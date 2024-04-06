package com.example.workflow.db.dao;

import com.example.workflow.db.pojo.TbLeave;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_leave】的数据库操作Mapper
* @createDate 2023-07-01 15:53:58
* @Entity com.example.workflow.db.pojo.TbLeave
*/
public interface TbLeaveMapper extends BaseMapper<TbLeave> {

    /**
     * 根据工作流实例id查询请假记录信息
     * @param instanceId 工作流实例id
     * @return 请假记录信息
     */
    HashMap<String, Object> searchLeaveByInstanceId(String instanceId);
}




