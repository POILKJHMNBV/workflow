package com.example.workflow.service;

import com.example.workflow.db.pojo.TbReim;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_reim(报销单表)】的数据库操作Service
* @createDate 2023-07-03 20:19:05
*/
public interface TbReimService extends IService<TbReim> {

    /**
     * 根据工作流实例id查询报销记录信息
     * @param instanceId 工作流实例id
     * @return 请假记录信息
     */
    HashMap<String, Object> searchReimByInstanceId(String instanceId);

}
