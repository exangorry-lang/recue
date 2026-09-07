package com.shrescue.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.service.TrainCheckinService;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.RequireRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 实训打卡接口（打卡提交、进度、多级审核）
 */
@RestController
@RequestMapping("/train/checkin")
public class TrainCheckinController {

    @Autowired
    private TrainCheckinService checkinService;

    @PostMapping
    public Result<Void> submit(@RequestBody TrainCheckin checkin) {
        checkinService.submit(checkin);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<Page<TrainCheckin>> my(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        return Result.ok(checkinService.my(page, size));
    }

    @GetMapping("/my-project")
    public Result<java.util.List<TrainCheckin>> myByProject(@RequestParam Long projectId) {
        return Result.ok(checkinService.myByProject(projectId));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/review")
    public Result<Page<TrainCheckin>> review(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.ok(checkinService.review(page, size));
    }

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer result = (Integer) body.get("result");
        String comment = (String) body.get("comment");
        checkinService.review(id, result, comment);
        return Result.ok();
    }
}
