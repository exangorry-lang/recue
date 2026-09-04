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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
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
}
