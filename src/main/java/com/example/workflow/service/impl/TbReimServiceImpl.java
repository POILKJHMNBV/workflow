package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.pojo.TbReim;
import com.example.workflow.service.TbReimService;
import com.example.workflow.db.dao.TbReimMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_reim(报销单表)】的数据库操作Service实现
* @createDate 2023-07-03 20:19:05
*/
@Service
public class TbReimServiceImpl extends ServiceImpl<TbReimMapper, TbReim>
    implements TbReimService {

    @Resource
    private TbReimMapper tbReimMapper;

    @Override
    public HashMap<String, Object> searchReimByInstanceId(String instanceId) {
        return tbReimMapper.searchReimByInstanceId(instanceId);
    }
}
