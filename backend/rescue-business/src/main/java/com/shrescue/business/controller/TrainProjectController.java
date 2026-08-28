package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.TrainMaterial;
import com.shrescue.business.entity.TrainProject;
import com.shrescue.business.mapper.TrainMaterialMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.business.util.LevelAuth;
import com.shrescue.common.core.Result;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分级训练项目接口（内容动态下发）
 */
@RestController
@RequestMapping("/train/project")
public class TrainProjectController {

    @Autowired
    private TrainProjectMapper projectMapper;
    @Autowired
    private TrainMaterialMapper materialMapper;

    @GetMapping("/page")
    public Result<Page<TrainProject>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) Integer level,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<TrainProject> qw = new LambdaQueryWrapper<>();
        qw.eq(TrainProject::getStatus, 1);
        if (level != null) {
            qw.eq(TrainProject::getLevel, level);
        }
        if (StringUtils.hasText(keyword)) {
            qw.like(TrainProject::getName, keyword);
        }
        // 等级鉴权：救生员仅可见本级及以下
        if (LevelAuth.isRescuer(UserContext.get())) {
            qw.le(TrainProject::getLevel, UserContext.getLevel());
        }
        qw.orderByAsc(TrainProject::getLevel).orderByAsc(TrainProject::getSort).orderByAsc(TrainProject::getId);
        return Result.ok(projectMapper.selectPage(new Page<>(page, size), qw));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        TrainProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("训练项目不存在");
        }
        // 等级鉴权
        if (LevelAuth.isRescuer(UserContext.get()) && UserContext.getLevel() != null
                && project.getLevel() > UserContext.getLevel()) {
            throw new BusinessException("等级不足，暂未开放");
        }
        List<TrainMaterial> materials = materialMapper.selectList(
                new LambdaQueryWrapper<TrainMaterial>()
                        .eq(TrainMaterial::getProjectId, id)
                        .orderByAsc(TrainMaterial::getSort));
        Map<String, Object> map = new HashMap<>();
        map.put("project", project);
        map.put("materials", materials);
        return Result.ok(map);
    }
}
