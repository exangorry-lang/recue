package com.shrescue.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.common.core.Result;
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

    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(page, size, keyword));
    }

    @GetMapping("/all")
    public Result<java.util.List<SysUser>> all() {
        return Result.ok(userService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser user) {
        userService.create(user);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestParam Integer enabled) {
        userService.updateStatus(id, enabled);
        return Result.ok();
    }

    @PutMapping("/{id}/level")
    public Result<Void> level(@PathVariable Long id, @RequestParam Integer level) {
        userService.updateLevel(id, level);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok();
    }
}
