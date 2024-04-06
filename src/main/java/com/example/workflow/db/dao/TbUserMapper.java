package com.example.workflow.db.dao;

import com.example.workflow.db.pojo.TbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author zhenwu
* @description 针对表【tb_user(用户表)】的数据库操作Mapper
* @createDate 2023-07-03 20:19:05
*/
public interface TbUserMapper extends BaseMapper<TbUser> {

    /**
     * 查询参会人员的邮箱
     * @param meetingId 会议id
     * @return 会议参会人员邮箱
     */
    List<String> queryEmailMeetingMembers(long meetingId);
}