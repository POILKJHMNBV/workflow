package com.example.workflow.service;

import cn.hutool.json.JSONArray;
import java.util.HashMap;

public interface WorkflowService {

    /**
     * 开启会议工作流实例，添加相关定时器
     * @param param 工作流实例或定时器所需参数
     * @return 工作流实例id
     */
    String startMeetingProcess(HashMap<String, Object> param);

    /**
     * 开启员工请假工作流实例
     * @param param 工作流实例所需参数
     * @return 工作流实例id
     */
    String startLeaveProcess(HashMap<String, Object> param);

    /**
     * 开启员工报销工作流实例
     * @param param 工作流实例所需参数
     * @return 工作流实例id
     */
    String startReimProcess(HashMap<String, Object> param);

    /**
     * 审批任务
     * @param taskId 任务id，ACT_RU_TASK 中的ID_
     * @param approval 审批结果(同意|不同意)
     */
    void approvalTask(String taskId, String approval);

    /**
     * 归档任务
     * @param taskId 任务id，ACT_RU_TASK 中的ID_
     * @param userId HRid
     * @param files 待归档的文件
     */
    void archiveTask(String taskId, Integer userId, JSONArray files);

    /**
     * 删除工作流实例和与该实例有关的定时器和触发器
     * @param uuid 会议uuid
     * @param instanceId 工作流实例id
     * @param type 类型(会议)
     * @param reason 删除原因
     */
    void deleteProcessById(String uuid, String instanceId, String type, String reason);

    /**
     * 分页查询审批任务信息
     * @param param 查询条件
     * @return 审批任务信息
     */
    HashMap<String, Object> searchTaskByPage(HashMap<String, Object> param);

    /**
     * 查询审批任务详情信息
     * @param instanceId 工作流实例id
     * @param userId 用户id
     * @param role 用户角色
     * @param type 审批任务类型(员工请假|会议申请|报销申请)
     * @param status 任务状态
     * @return 审批任务详情信息
     */
    HashMap<String, Object> searchApprovalContent(String instanceId, int userId, String[] role, String type, String status);

    /**
     * 审批流程递转
     * @param userId 离职人员id
     * @param assignId 交接人员id
     */
    void turnTask(int userId, int assignId);

    /**
     * 查询用户的审批任务数目
     * @param userId 用户id
     * @return 用户的审批任务数目
     */
    long searchApprovalTaskCount(int userId);
}
