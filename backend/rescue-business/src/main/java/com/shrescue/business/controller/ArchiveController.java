package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.business.entity.PromotionRecord;
import com.shrescue.business.entity.UserArchive;
import com.shrescue.business.mapper.PromotionRecordMapper;
import com.shrescue.business.mapper.UserArchiveMapper;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.Result;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.RequireRole;
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
        if (me.getLevel() == null || me.getLevel() >= 5) {
            throw new BusinessException("当前已是最高等级，不能继续申请晋升");
        }
        if (record.getToLevel() == null || !record.getToLevel().equals(me.getLevel() + 1)) {
            throw new BusinessException("晋升申请只能提升一个等级");
        }
        Long pending = promotionMapper.selectCount(new LambdaQueryWrapper<PromotionRecord>()
                .eq(PromotionRecord::getUserId, me.getUserId()).eq(PromotionRecord::getStatus, 0));
        if (pending != null && pending > 0) {
            throw new BusinessException("已有待审批的晋升申请");
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

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/promotion/pending")
    public Result<List<PromotionRecord>> pendingList() {
        List<PromotionRecord> records = promotionMapper.selectList(
                new LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getStatus, 0)
                        .orderByAsc(PromotionRecord::getId));
        records.removeIf(record -> !canManageUser(record.getUserId()));
        return Result.ok(records);
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/promotion/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PromotionRecord record = promotionMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("晋升申请不存在");
        }
        if (!Integer.valueOf(0).equals(record.getStatus())) {
            throw new BusinessException("该申请已处理，不能重复审批");
        }
        if (!canManageUser(record.getUserId())) {
            throw new BusinessException("无权审批其他部门的晋升申请");
        }
        Integer result = (Integer) body.get("result");
        if (result == null || (result != 1 && result != 2)) {
            throw new BusinessException("审批结果非法");
        }
        SysUser applicant = userMapper.selectById(record.getUserId());
        if (applicant == null || !record.getFromLevel().equals(applicant.getRescueLevel())
                || !record.getToLevel().equals(record.getFromLevel() + 1)) {
            throw new BusinessException("申请等级状态已变化，请重新发起申请");
        }
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

    private boolean canManageUser(Long userId) {
        SysUser target = userMapper.selectById(userId);
        return target != null && ScopeUtil.canAccessDept(UserContext.get(), target.getDeptId());
    }
}
