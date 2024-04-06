package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.dao.TbAmectMapper;
import com.example.workflow.db.pojo.TbAmect;
import com.example.workflow.service.TbAmectService;
import org.springframework.stereotype.Service;

/**
* @author zhenwu
* @description 针对表【tb_amect(罚金表)】的数据库操作Service实现
* @createDate 2023-06-04 21:05:52
*/
@Service
public class TbAmectServiceImpl extends ServiceImpl<TbAmectMapper, TbAmect>
    implements TbAmectService {

}