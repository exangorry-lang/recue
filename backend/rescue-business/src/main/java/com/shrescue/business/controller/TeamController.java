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
import com.shrescue.common.core.Result;
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

    @PostMapping("/group")
    public Result<Void> createGroup(@RequestBody TeamGroup group) {
        group.setId(null);
        group.setCreateBy(UserContext.getUserId());
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        groupMapper.insert(group);
        return Result.ok();
    }

    @GetMapping("/task/list")
    public Result<List<TeamTask>> taskList() {
        return Result.ok(taskMapper.selectList(
                new LambdaQueryWrapper<TeamTask>().orderByDesc(TeamTask::getId)));
    }

    @PostMapping("/task")
    public Result<Void> createTask(@RequestBody TeamTask task) {
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
        return Result.ok(recordMapper.selectList(
                new LambdaQueryWrapper<TeamRecord>()
                        .eq(TeamRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(TeamRecord::getId)));
    }

    /** 查询任务复盘/问题台账 */
    @GetMapping("/review/{taskId}")
    public Result<List<TeamReview>> reviews(@org.springframework.web.bind.annotation.PathVariable Long taskId) {
        return Result.ok(reviewMapper.selectList(
                new LambdaQueryWrapper<TeamReview>()
                        .eq(TeamReview::getTaskId, taskId)
                        .orderByDesc(TeamReview::getId)));
    }

    /** 提交复盘/问题台账 */
    @PostMapping("/review")
    public Result<Void> addReview(@RequestBody TeamReview review) {
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
    @GetMapping("/task/{taskId}/records")
    public Result<List<TeamRecord>> taskRecords(@PathVariable Long taskId) {
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
    @PostMapping("/record/{id}/score")
    public Result<Void> score(@PathVariable Long id, @RequestBody TeamRecord body) {
        TeamRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new com.shrescue.common.exception.BusinessException("参与记录不存在");
        }
        record.setContribution(body.getContribution());
        record.setComment(body.getComment());
        record.setStatus(2);
        record.setUpdateTime(new Date());
        recordMapper.updateById(record);
        return Result.ok();
    }
}
