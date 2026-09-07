package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.business.entity.TeamGroup;
import com.shrescue.business.entity.TeamGroupMember;
import com.shrescue.business.entity.TeamRecord;
import com.shrescue.business.entity.TeamReview;
import com.shrescue.business.entity.TeamTask;
import com.shrescue.business.mapper.TeamGroupMapper;
import com.shrescue.business.mapper.TeamGroupMemberMapper;
import com.shrescue.business.mapper.TeamRecordMapper;
import com.shrescue.business.mapper.TeamReviewMapper;
import com.shrescue.business.mapper.TeamTaskMapper;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.RequireRole;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.ScopeUtil;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 团队协同救援训练接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamGroupMapper groupMapper;
    @Autowired
    private TeamGroupMemberMapper memberMapper;
    @Autowired
    private TeamTaskMapper taskMapper;
    @Autowired
    private TeamRecordMapper recordMapper;
    @Autowired
    private TeamReviewMapper reviewMapper;
    @Autowired
    private SysUserMapper userMapper;

    @GetMapping("/group/list")
    public Result<List<TeamGroup>> groupList() {
        return Result.ok(groupMapper.selectList(
                new LambdaQueryWrapper<TeamGroup>().orderByAsc(TeamGroup::getId)));
    }

    @GetMapping("/group/my")
    public Result<List<TeamGroup>> myGroups() {
        List<TeamGroupMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<TeamGroupMember>().eq(TeamGroupMember::getUserId, UserContext.getUserId()));
        if (members.isEmpty()) {
            return Result.ok(List.of());
        }
        List<Long> groupIds = members.stream().map(TeamGroupMember::getGroupId).collect(Collectors.toList());
        return Result.ok(groupMapper.selectBatchIds(groupIds));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/group")
    public Result<Void> createGroup(@RequestBody TeamGroup group) {
        LoginUser me = UserContext.get();
        if (ScopeUtil.hasDeptScope(me)) {
            if (group.getDeptId() == null) {
                group.setDeptId(me.getDeptId());
            }
            if (!ScopeUtil.canAccessDept(me, group.getDeptId())) {
                throw new com.shrescue.common.exception.BusinessException("不能创建其他部门的团队");
            }
        }
        if (group.getLeaderId() != null) {
            SysUser leader = userMapper.selectById(group.getLeaderId());
            if (leader == null) {
                throw new com.shrescue.common.exception.BusinessException("负责人不存在");
            }
            if (leader.getRescueLevel() == null || leader.getRescueLevel() < 5) {
                throw new com.shrescue.common.exception.BusinessException("团队负责人必须是5级救生员");
            }
            if (!ScopeUtil.canAccessDept(me, leader.getDeptId())) {
                throw new com.shrescue.common.exception.BusinessException("团队负责人必须属于可管理的部门");
            }
        }
        group.setId(null);
        group.setCreateBy(UserContext.getUserId());
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        groupMapper.insert(group);
        return Result.ok();
    }

    @GetMapping("/task/list")
    public Result<List<TeamTask>> taskList() {
        LoginUser me = UserContext.get();
        LambdaQueryWrapper<TeamTask> qw = new LambdaQueryWrapper<>();
        if (!ScopeUtil.hasAllScope(me)) {
            List<TeamGroup> groups;
            if (ScopeUtil.hasDeptScope(me)) {
                groups = groupMapper.selectList(new LambdaQueryWrapper<TeamGroup>().eq(TeamGroup::getDeptId, me.getDeptId()));
            } else {
                List<TeamGroupMember> memberships = memberMapper.selectList(
                        new LambdaQueryWrapper<TeamGroupMember>().eq(TeamGroupMember::getUserId, me.getUserId()));
                if (memberships.isEmpty()) {
                    return Result.ok(List.of());
                }
                groups = groupMapper.selectBatchIds(memberships.stream().map(TeamGroupMember::getGroupId).toList());
            }
            List<Long> groupIds = groups.stream().map(TeamGroup::getId).toList();
            if (groupIds.isEmpty()) {
                return Result.ok(List.of());
            }
            qw.in(TeamTask::getGroupId, groupIds);
        }
        return Result.ok(taskMapper.selectList(qw.orderByDesc(TeamTask::getId)));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/task")
    public Result<Void> createTask(@RequestBody TeamTask task) {
        if (task.getGroupId() == null || !canAccessGroup(task.getGroupId())) {
            throw new com.shrescue.common.exception.BusinessException("无权向该团队下发任务");
        }
        task.setId(null);
        task.setIssuerId(UserContext.getUserId());
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        task.setCreateBy(UserContext.getUserId());
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        taskMapper.insert(task);
        return Result.ok();
    }

    @PostMapping("/record")
    public Result<Void> submitRecord(@RequestBody TeamRecord record) {
        if (record.getTaskId() == null) {
            throw new com.shrescue.common.exception.BusinessException("请选择团队任务");
        }
        TeamTask task = taskMapper.selectById(record.getTaskId());
        if (task == null || !Integer.valueOf(1).equals(task.getStatus()) || !isMember(task.getGroupId(), UserContext.getUserId())) {
            throw new com.shrescue.common.exception.BusinessException("该团队任务不存在、未开放或您未被编入团队");
        }
        Long exist = recordMapper.selectCount(
                new LambdaQueryWrapper<TeamRecord>()
                        .eq(TeamRecord::getTaskId, record.getTaskId())
                        .eq(TeamRecord::getUserId, UserContext.getUserId()));
        if (exist != null && exist > 0) {
            throw new com.shrescue.common.exception.BusinessException("已参与过该任务，请勿重复打卡");
        }
        record.setId(null);
        record.setUserId(UserContext.getUserId());
        if (record.getCheckinTime() == null) {
            record.setCheckinTime(new Date());
        }
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        if (record.getOfflineFlag() == null) {
            record.setOfflineFlag(0);
        }
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        recordMapper.insert(record);
        return Result.ok();
    }

    @GetMapping("/record/my")
    public Result<List<TeamRecord>> myRecords() {
        List<TeamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<TeamRecord>()
                        .eq(TeamRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(TeamRecord::getId));
        if (!records.isEmpty()) {
            List<Long> taskIds = records.stream().map(TeamRecord::getTaskId).distinct().collect(Collectors.toList());
            Map<Long, String> taskNameMap = new HashMap<>();
            for (TeamTask t : taskMapper.selectBatchIds(taskIds)) {
                taskNameMap.put(t.getId(), t.getName());
            }
            records.forEach(r -> r.setTaskName(taskNameMap.getOrDefault(r.getTaskId(), String.valueOf(r.getTaskId()))));
        }
        return Result.ok(records);
    }

    /** 查询任务复盘/问题台账 */
    @GetMapping("/review/{taskId}")
    public Result<List<TeamReview>> reviews(@org.springframework.web.bind.annotation.PathVariable Long taskId) {
        if (!canAccessTask(taskId)) {
            throw new com.shrescue.common.exception.BusinessException("无权查看该团队任务");
        }
        return Result.ok(reviewMapper.selectList(
                new LambdaQueryWrapper<TeamReview>()
                        .eq(TeamReview::getTaskId, taskId)
                        .orderByDesc(TeamReview::getId)));
    }

    /** 提交复盘/问题台账 */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/review")
    public Result<Void> addReview(@RequestBody TeamReview review) {
        if (review.getTaskId() == null || !canAccessTask(review.getTaskId())) {
            throw new com.shrescue.common.exception.BusinessException("无权复盘该团队任务");
        }
        review.setId(null);
        review.setReviewerId(UserContext.getUserId());
        if (review.getReviewTime() == null) {
            review.setReviewTime(new Date());
        }
        review.setCreateTime(new Date());
        review.setUpdateTime(new Date());
        reviewMapper.insert(review);
        return Result.ok();
    }

    /** 查看任务参与记录（含队员姓名） */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/task/{taskId}/records")
    public Result<List<TeamRecord>> taskRecords(@PathVariable Long taskId) {
        if (!canAccessTask(taskId)) {
            throw new com.shrescue.common.exception.BusinessException("无权查看该团队任务");
        }
        List<TeamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<TeamRecord>()
                        .eq(TeamRecord::getTaskId, taskId)
                        .orderByDesc(TeamRecord::getId));
        if (!records.isEmpty()) {
            List<Long> userIds = records.stream().map(TeamRecord::getUserId).distinct().collect(Collectors.toList());
            Map<Long, String> nameMap = new HashMap<>();
            for (SysUser u : userMapper.selectBatchIds(userIds)) {
                nameMap.put(u.getId(), u.getRealName());
            }
            records.forEach(r -> r.setUserName(nameMap.getOrDefault(r.getUserId(), String.valueOf(r.getUserId()))));
        }
        return Result.ok(records);
    }

    /** 给参与记录打分（个人贡献评分） */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/record/{id}/score")
    public Result<Void> score(@PathVariable Long id, @RequestBody TeamRecord body) {
        TeamRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new com.shrescue.common.exception.BusinessException("参与记录不存在");
        }
        TeamTask task = taskMapper.selectById(record.getTaskId());
        if (task == null || !canAccessTask(task.getId())) {
            throw new com.shrescue.common.exception.BusinessException("无权给该团队记录评分");
        }
        if (body.getContribution() != null
                && (body.getContribution().signum() < 0
                    || body.getContribution().compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            throw new com.shrescue.common.exception.BusinessException("贡献评分应在0至100之间");
        }
        record.setContribution(body.getContribution());
        record.setComment(body.getComment());
        record.setStatus(2);
        record.setUpdateTime(new Date());
        recordMapper.updateById(record);
        return Result.ok();
    }

    private boolean canAccessTask(Long taskId) {
        TeamTask task = taskMapper.selectById(taskId);
        return task != null && canAccessGroup(task.getGroupId());
    }

    private boolean canAccessGroup(Long groupId) {
        TeamGroup group = groupMapper.selectById(groupId);
        if (group == null) return false;
        LoginUser me = UserContext.get();
        return ScopeUtil.canAccessDept(me, group.getDeptId()) || isMember(groupId, me.getUserId());
    }

    private boolean isMember(Long groupId, Long userId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<TeamGroupMember>()
                .eq(TeamGroupMember::getGroupId, groupId).eq(TeamGroupMember::getUserId, userId));
        return count != null && count > 0;
    }
}
