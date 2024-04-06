package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.dao.TbAmectTypeMapper;
import com.example.workflow.db.pojo.TbAmectType;
import com.example.workflow.service.TbAmectTypeService;
import org.springframework.stereotype.Service;

/**
* @author zhenwu
* @description 针对表【tb_amect_type(罚金类型表)】的数据库操作Service实现
* @createDate 2023-06-04 21:05:52
*/
@Service
public class TbAmectTypeServiceImpl extends ServiceImpl<TbAmectTypeMapper, TbAmectType>
    implements TbAmectTypeService {

}