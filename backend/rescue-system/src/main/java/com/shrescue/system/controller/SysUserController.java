package com.shrescue.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.RequireRole;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(page, size, keyword));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/all")
    public Result<java.util.List<SysUser>> all() {
        return Result.ok(userService.listAll());
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping
    public Result<Void> create(@RequestBody SysUser user) {
        userService.create(user);
        return Result.ok();
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.ok();
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer enabled) {
        userService.updateStatus(id, enabled);
        return Result.ok();
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PutMapping("/{id}/level")
    public Result<Void> level(@PathVariable Long id, @RequestParam Integer level) {
        userService.updateLevel(id, level);
        return Result.ok();
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword"));
        return Result.ok();
    }
}
