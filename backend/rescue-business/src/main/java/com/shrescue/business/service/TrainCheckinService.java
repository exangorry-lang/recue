package com.shrescue.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.entity.TrainCheckinReview;
import com.shrescue.business.entity.TrainProject;
import com.shrescue.business.mapper.TrainCheckinMapper;
import com.shrescue.business.mapper.TrainCheckinReviewMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.ScopeUtil;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练打卡服务
 */
@Service
public class TrainCheckinService {

    @Autowired
    private TrainCheckinMapper checkinMapper;
    @Autowired
    private TrainCheckinReviewMapper reviewMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private TrainProjectMapper projectMapper;

    public void submit(TrainCheckin checkin) {
        if (checkin.getProjectId() == null) {
            throw new BusinessException("请选择训练项目");
        }
        TrainProject project = projectMapper.selectById(checkin.getProjectId());
        if (project == null || !Integer.valueOf(1).equals(project.getStatus())) {
            throw new BusinessException("训练项目不存在或未开放");
        }
        LoginUser me = UserContext.get();
        if (me.getLevel() != null && project.getLevel() != null && project.getLevel() > me.getLevel()) {
            throw new BusinessException("当前等级不能提交该训练项目");
        }
        if (checkin.getSelfEval() == null || checkin.getSelfEval() < 1 || checkin.getSelfEval() > 3) {
            throw new BusinessException("自评结果非法");
        }
        if (checkin.getProgress() == null || checkin.getProgress() < 0 || checkin.getProgress() > 100) {
            throw new BusinessException("训练进度应在0至100之间");
        }
        Long pending = checkinMapper.selectCount(
                new LambdaQueryWrapper<TrainCheckin>()
                        .eq(TrainCheckin::getUserId, UserContext.getUserId())
                        .eq(TrainCheckin::getProjectId, checkin.getProjectId())
                        .eq(TrainCheckin::getStatus, 0));
        if (pending != null && pending > 0) {
            throw new BusinessException("该训练已有待审核的打卡，请等待审核结果");
        }
        checkin.setId(null);
        checkin.setUserId(UserContext.getUserId());
        checkin.setCheckinTime(new Date());
        checkin.setStatus(0);
        if (checkin.getProgress() == null) {
            checkin.setProgress(100);
        }
        if (checkin.getOfflineFlag() == null) {
            checkin.setOfflineFlag(0);
        }
        checkin.setCreateBy(UserContext.getUserId());
        checkin.setCreateTime(new Date());
        checkin.setUpdateTime(new Date());
        checkinMapper.insert(checkin);
    }

    public Page<TrainCheckin> my(long page, long size) {
        LambdaQueryWrapper<TrainCheckin> qw = new LambdaQueryWrapper<>();
        qw.eq(TrainCheckin::getUserId, UserContext.getUserId())
                .orderByDesc(TrainCheckin::getId);
        return checkinMapper.selectPage(new Page<>(page, size), qw);
    }

    /** 我的某训练项目的打卡记录（供详情页展示审核状态） */
    public java.util.List<TrainCheckin> myByProject(Long projectId) {
        return checkinMapper.selectList(
                new LambdaQueryWrapper<TrainCheckin>()
                        .eq(TrainCheckin::getUserId, UserContext.getUserId())
                        .eq(TrainCheckin::getProjectId, projectId)
                        .orderByDesc(TrainCheckin::getId));
    }

    public Page<TrainCheckin> review(long page, long size) {
        LambdaQueryWrapper<TrainCheckin> qw = new LambdaQueryWrapper<>();
        qw.eq(TrainCheckin::getStatus, 0);
        LoginUser me = UserContext.get();
        // 部门负责人仅查看本部门队员的打卡（数据隔离）
        if (me != null && me.getDataScope() != null && me.getDataScope() == Constants.DATA_SCOPE_DEPT) {
            List<SysUser> deptUsers = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, me.getDeptId()));
            List<Long> userIds = deptUsers.stream().map(SysUser::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                qw.eq(TrainCheckin::getId, -1L);
            } else {
                qw.in(TrainCheckin::getUserId, userIds);
            }
        }
        qw.orderByAsc(TrainCheckin::getId);
        return checkinMapper.selectPage(new Page<>(page, size), qw);
    }

    public void review(Long checkinId, Integer result, String comment) {
        TrainCheckin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("打卡记录不存在");
        }
        if (result == null || (result != 1 && result != 2)) {
            throw new BusinessException("审核结果非法");
        }
        if (!Integer.valueOf(0).equals(checkin.getStatus())) {
            throw new BusinessException("该打卡已审核，不能重复处理");
        }
        SysUser owner = userMapper.selectById(checkin.getUserId());
        if (owner == null || !ScopeUtil.canAccessDept(UserContext.get(), owner.getDeptId())) {
            throw new BusinessException("无权审核其他部门的打卡");
        }
        checkin.setStatus(result);
        checkin.setUpdateBy(UserContext.getUserId());
        checkin.setUpdateTime(new Date());
        checkinMapper.updateById(checkin);

        TrainCheckinReview review = new TrainCheckinReview();
        review.setCheckinId(checkinId);
        review.setReviewerId(UserContext.getUserId());
        review.setReviewLevel(1);
        review.setResult(result);
        review.setComment(comment);
        review.setReviewTime(new Date());
        reviewMapper.insert(review);
    }
}
