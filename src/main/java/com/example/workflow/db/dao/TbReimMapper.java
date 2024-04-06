package com.example.workflow.db.dao;

import com.example.workflow.db.pojo.TbReim;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_reim(报销单表)】的数据库操作Mapper
* @createDate 2023-07-03 20:19:05
* @Entity com.example.workflow.db.pojo.TbReim
*/
public interface TbReimMapper extends BaseMapper<TbReim> {

    /**
     * 根据工作流实例id查询报销记录信息
     * @param instanceId 工作流实例id
     * @return 请假记录信息
     */
    HashMap<String, Object> searchReimByInstanceId(String instanceId);
}




