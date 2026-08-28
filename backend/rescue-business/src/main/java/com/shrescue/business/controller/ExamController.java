package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.ExamScore;
import com.shrescue.business.entity.ExamSession;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.ExamSessionMapper;
import com.shrescue.business.service.ExamService;
import com.shrescue.business.util.LevelAuth;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 考试接口（题库练习、随机组卷、等级考试判分）
 */
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;
    @Autowired
    private ExamSessionMapper sessionMapper;
    @Autowired
    private ExamScoreMapper scoreMapper;

    @GetMapping("/session/list")
    public Result<List<ExamSession>> sessionList(@RequestParam(required = false) Integer level) {
        LambdaQueryWrapper<ExamSession> qw = new LambdaQueryWrapper<>();
        if (level != null) {
            qw.eq(ExamSession::getLevel, level);
        }
        if (LevelAuth.isRescuer(UserContext.get())) {
            qw.le(ExamSession::getLevel, UserContext.getLevel());
        }
        qw.orderByDesc(ExamSession::getId);
        return Result.ok(sessionMapper.selectList(qw));
    }

    @PostMapping("/session")
    public Result<Void> createSession(@RequestBody ExamSession session) {
        examService.createSession(session);
        return Result.ok();
    }

    @PostMapping("/session/{id}/start")
    public Result<Map<String, Object>> start(@PathVariable Long id) {
        return Result.ok(examService.start(id));
    }

    @PostMapping("/record/{recordId}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long recordId,
                                              @RequestBody List<com.shrescue.business.dto.AnswerItem> answers) {
        return Result.ok("交卷成功", examService.submit(recordId, answers));
    }

    @GetMapping("/score/my")
    public Result<List<ExamScore>> myScore() {
        return Result.ok(scoreMapper.selectList(new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getUserId, UserContext.getUserId())
                .orderByDesc(ExamScore::getId)));
    }
}
