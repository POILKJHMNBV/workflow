package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.pojo.TbLeave;
import com.example.workflow.service.TbLeaveService;
import com.example.workflow.db.dao.TbLeaveMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
* @author zhenwu
* @description 针对表【tb_leave】的数据库操作Service实现
* @createDate 2023-07-01 15:53:58
*/
@Service
public class TbLeaveServiceImpl extends ServiceImpl<TbLeaveMapper, TbLeave>
    implements TbLeaveService {

    @Resource
    private TbLeaveMapper tbLeaveMapper;

    @Override
    public HashMap<String, Object> searchLeaveByInstanceId(String instanceId) {
        return tbLeaveMapper.searchLeaveByInstanceId(instanceId);
    }
}




