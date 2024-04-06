package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.db.pojo.TbUser;
import com.example.workflow.service.TbUserService;
import com.example.workflow.db.dao.TbUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zhenwu
* @description 针对表【tb_user(用户表)】的数据库操作Service实现
* @createDate 2023-07-03 20:19:05
*/
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser>
    implements TbUserService{

}