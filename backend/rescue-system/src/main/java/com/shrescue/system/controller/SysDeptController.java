package com.shrescue.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.common.core.Result;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysDept;
import com.shrescue.system.mapper.SysDeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 部门/班组管理接口
 */
@RestController
@RequestMapping("/sys/dept")
public class SysDeptController {

    @Autowired
    private SysDeptMapper deptMapper;

    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        List<SysDept> list = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSort).orderByAsc(SysDept::getId));
        return Result.ok(list);
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysDept dept) {
        dept.setId(null);
        dept.setCreateBy(UserContext.getUserId());
        dept.setCreateTime(new Date());
        dept.setUpdateTime(new Date());
        deptMapper.insert(dept);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysDept dept) {
        dept.setUpdateBy(UserContext.getUserId());
        dept.setUpdateTime(new Date());
        deptMapper.updateById(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long children = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException("存在下级组织，无法删除");
        }
        deptMapper.deleteById(id);
        return Result.ok();
    }
}
