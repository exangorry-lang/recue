package com.shrescue.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.common.core.Result;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.aspect.OpLog;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysNotice;
import com.shrescue.system.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 通知公告接口
 */
@RestController
@RequestMapping("/sys/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeMapper noticeMapper;

    /** 已发布公告列表（双端可见） */
    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        return Result.ok(noticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>()
                        .eq(SysNotice::getStatus, 1)
                        .orderByDesc(SysNotice::getPublishTime)
                        .orderByDesc(SysNotice::getId)));
    }

    /** 管理端分页（含草稿） */
    @GetMapping("/page")
    public Result<Page<SysNotice>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysNotice> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(SysNotice::getTitle, keyword);
        }
        qw.orderByDesc(SysNotice::getId);
        return Result.ok(noticeMapper.selectPage(new Page<>(page, size), qw));
    }

    @OpLog(module = "通知公告", action = "新增通知公告")
    @PostMapping
    public Result<Void> create(@RequestBody SysNotice notice) {
        notice.setId(null);
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        if (notice.getNoticeType() == null) {
            notice.setNoticeType(1);
        }
        if (notice.getStatus() == 1 && notice.getPublishTime() == null) {
            notice.setPublishTime(new Date());
        }
        notice.setCreateBy(UserContext.getUserId());
        notice.setCreateTime(new Date());
        notice.setUpdateTime(new Date());
        noticeMapper.insert(notice);
        return Result.ok();
    }

    @OpLog(module = "通知公告", action = "修改通知公告")
    @PutMapping
    public Result<Void> update(@RequestBody SysNotice notice) {
        if (notice.getId() == null) {
            throw new BusinessException("缺少ID");
        }
        notice.setUpdateBy(UserContext.getUserId());
        notice.setUpdateTime(new Date());
        noticeMapper.updateById(notice);
        return Result.ok();
    }

    @OpLog(module = "通知公告", action = "删除通知公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.ok();
    }
}
