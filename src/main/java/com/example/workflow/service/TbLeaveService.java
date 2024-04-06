package com.example.workflow.service;

import com.example.workflow.db.pojo.TbLeave;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_leave】的数据库操作Service
* @createDate 2023-07-01 15:53:58
*/
public interface TbLeaveService extends IService<TbLeave> {
    /**
     * 根据工作流实例id查询请假记录信息
     * @param instanceId 工作流实例id
     * @return 请假记录信息
     */
    HashMap<String, Object> searchLeaveByInstanceId(String instanceId);
}
