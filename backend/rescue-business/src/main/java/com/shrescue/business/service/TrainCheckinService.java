package com.shrescue.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.entity.TrainCheckinReview;
import com.shrescue.business.mapper.TrainCheckinMapper;
import com.shrescue.business.mapper.TrainCheckinReviewMapper;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 训练打卡服务
 */
@Service
public class TrainCheckinService {

    @Autowired
    private TrainCheckinMapper checkinMapper;
    @Autowired
    private TrainCheckinReviewMapper reviewMapper;

    public void submit(TrainCheckin checkin) {
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
        qw.eq(TrainCheckin::getStatus, 0).orderByAsc(TrainCheckin::getId);
        return checkinMapper.selectPage(new Page<>(page, size), qw);
    }

    public void review(Long checkinId, Integer result, String comment) {
        TrainCheckin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("打卡记录不存在");
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
