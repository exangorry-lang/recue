package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.business.entity.PromotionRecord;
import com.shrescue.business.entity.UserArchive;
import com.shrescue.business.mapper.PromotionRecordMapper;
import com.shrescue.business.mapper.UserArchiveMapper;
import com.shrescue.common.core.Result;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.LoginUser;
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
import java.util.List;
import java.util.Map;

/**
 * 个人档案与晋升台账接口
 */
@RestController
@RequestMapping("/archive")
public class ArchiveController {

    @Autowired
    private UserArchiveMapper archiveMapper;
    @Autowired
    private PromotionRecordMapper promotionMapper;
    @Autowired
    private SysUserMapper userMapper;

    @GetMapping("/my")
    public Result<UserArchive> my() {
        UserArchive archive = archiveMapper.selectOne(
                new LambdaQueryWrapper<UserArchive>().eq(UserArchive::getUserId, UserContext.getUserId()));
        return Result.ok(archive);
    }

    @PostMapping("/promotion")
    public Result<Void> applyPromotion(@RequestBody PromotionRecord record) {
        LoginUser me = UserContext.get();
        if (record.getToLevel() == null || record.getToLevel() < 1 || record.getToLevel() > 5) {
            throw new BusinessException("目标等级非法");
        }
        record.setId(null);
        record.setUserId(me.getUserId());
        record.setFromLevel(me.getLevel());
        record.setStatus(0);
        record.setApplyBy(me.getUserId());
        record.setApplyTime(new Date());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        promotionMapper.insert(record);
        return Result.ok();
    }

    @GetMapping("/promotion/list")
    public Result<List<PromotionRecord>> promotionList() {
        return Result.ok(promotionMapper.selectList(
                new LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(PromotionRecord::getId)));
    }

    @GetMapping("/promotion/pending")
    public Result<List<PromotionRecord>> pendingList() {
        return Result.ok(promotionMapper.selectList(
                new LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getStatus, 0)
                        .orderByAsc(PromotionRecord::getId)));
    }

    @PostMapping("/promotion/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PromotionRecord record = promotionMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("晋升申请不存在");
        }
        Integer result = (Integer) body.get("result");
        record.setStatus(result);
        record.setApproveBy(UserContext.getUserId());
        record.setApproveTime(new Date());
        record.setUpdateTime(new Date());
        promotionMapper.updateById(record);

        if (result != null && result == 1) {
            // 通过：更新用户等级与档案
            SysUser user = new SysUser();
            user.setId(record.getUserId());
            user.setRescueLevel(record.getToLevel());
            user.setUpdateBy(UserContext.getUserId());
            user.setUpdateTime(new Date());
            userMapper.updateById(user);

            UserArchive archive = archiveMapper.selectOne(
                    new LambdaQueryWrapper<UserArchive>().eq(UserArchive::getUserId, record.getUserId()));
            if (archive != null) {
                archive.setCurrentLevel(record.getToLevel());
                archive.setUpdateTime(new Date());
                archiveMapper.updateById(archive);
            }
        }
        return Result.ok();
    }
}
