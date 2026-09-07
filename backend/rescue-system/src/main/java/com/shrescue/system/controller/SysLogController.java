package com.shrescue.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.common.core.Result;
import com.shrescue.common.constant.Constants;
import com.shrescue.framework.security.RequireRole;
import com.shrescue.system.entity.SysLoginLog;
import com.shrescue.system.entity.SysOpLog;
import com.shrescue.system.mapper.SysLoginLogMapper;
import com.shrescue.system.mapper.SysOpLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志查询接口
 */
@RestController
@RequestMapping("/sys/log")
public class SysLogController {

    @Autowired
    private SysOpLogMapper opLogMapper;
    @Autowired
    private SysLoginLogMapper loginLogMapper;

    @RequireRole(Constants.ROLE_SUPER_ADMIN)
    @GetMapping("/op/page")
    public Result<Page<SysOpLog>> opPage(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String module) {
        LambdaQueryWrapper<SysOpLog> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            qw.like(SysOpLog::getModule, module);
        }
        qw.orderByDesc(SysOpLog::getId);
        return Result.ok(opLogMapper.selectPage(new Page<>(page, size), qw));
    }

    @RequireRole(Constants.ROLE_SUPER_ADMIN)
    @GetMapping("/login/page")
    public Result<Page<SysLoginLog>> loginPage(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysLoginLog> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            qw.like(SysLoginLog::getUsername, username);
        }
        qw.orderByDesc(SysLoginLog::getId);
        return Result.ok(loginLogMapper.selectPage(new Page<>(page, size), qw));
    }
}
