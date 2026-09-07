package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.ExamRecord;
import com.shrescue.business.entity.ExamScore;
import com.shrescue.business.entity.ExamSession;
import com.shrescue.business.mapper.ExamRecordMapper;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.ExamSessionMapper;
import com.shrescue.business.service.ExamService;
import com.shrescue.business.util.LevelAuth;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.RequireRole;
import com.shrescue.framework.security.LoginUser;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
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
    @Autowired
    private ExamRecordMapper recordMapper;
    @Autowired
    private SysUserMapper userMapper;

    @GetMapping("/session/list")
    public Result<List<ExamSession>> sessionList(@RequestParam(required = false) Integer level) {
        LambdaQueryWrapper<ExamSession> qw = new LambdaQueryWrapper<>();
        if (level != null) {
            qw.eq(ExamSession::getLevel, level);
        }
        if (LevelAuth.isRescuer(UserContext.get())) {
            qw.le(ExamSession::getLevel, UserContext.getLevel());
            qw.eq(ExamSession::getStatus, 1);
        }
        qw.orderByDesc(ExamSession::getId);
        return Result.ok(sessionMapper.selectList(qw));
    }

    @RequireRole(Constants.ROLE_SUPER_ADMIN)
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

    /** 待人工阅卷列表（含简答题、已交卷待阅卷） */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/review/pending")
    public Result<List<ExamRecord>> reviewPending() {
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStatus, 1)
                        .orderByAsc(ExamRecord::getId));
        records.removeIf(record -> !canManageUser(record.getUserId()));
        return Result.ok(records);
    }

    /** 人工阅卷（简答题） */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @PostMapping("/review/{recordId}")
    public Result<Void> review(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new com.shrescue.common.exception.BusinessException("考试记录不存在");
        }
        if (!Integer.valueOf(1).equals(record.getStatus())) {
            throw new com.shrescue.common.exception.BusinessException("该考试记录不在待阅卷状态");
        }
        if (!canManageUser(record.getUserId())) {
            throw new com.shrescue.common.exception.BusinessException("无权阅卷其他部门的考试");
        }
        Object rawScore = body.get("score");
        if (rawScore == null) {
            throw new com.shrescue.common.exception.BusinessException("请填写最终得分");
        }
        BigDecimal score;
        try {
            score = new BigDecimal(String.valueOf(rawScore));
        } catch (NumberFormatException e) {
            throw new com.shrescue.common.exception.BusinessException("最终得分格式不正确");
        }
        ExamSession session = sessionMapper.selectById(record.getSessionId());
        if (session == null) {
            throw new com.shrescue.common.exception.BusinessException("考试场次不存在");
        }
        BigDecimal totalScore = session.getTotalScore() == null ? new BigDecimal("100") : session.getTotalScore();
        BigDecimal objectiveScore = record.getScore() == null ? BigDecimal.ZERO : record.getScore();
        if (score.compareTo(objectiveScore) < 0 || score.compareTo(totalScore) > 0) {
            throw new com.shrescue.common.exception.BusinessException("最终得分应介于客观题得分和卷面总分之间");
        }
        BigDecimal passScore = session.getPassScore() == null ? new BigDecimal("60") : session.getPassScore();
        record.setScore(score);
        record.setIsPass(score.compareTo(passScore) >= 0 ? 1 : 0);
        record.setStatus(2);
        record.setReviewBy(UserContext.getUserId());
        record.setReviewTime(new Date());
        record.setUpdateTime(new Date());
        recordMapper.updateById(record);

        // 同步更新成绩归档（含简答题的人工阅卷后最终成绩）
        int isPass = score.compareTo(passScore) >= 0 ? 1 : 0;
        ExamScore exist = scoreMapper.selectOne(
                new LambdaQueryWrapper<ExamScore>().eq(ExamScore::getRecordId, recordId));
        if (exist != null) {
            exist.setScore(score);
            exist.setIsPass(isPass);
            scoreMapper.updateById(exist);
        } else {
            ExamScore scoreEntity = new ExamScore();
            scoreEntity.setRecordId(recordId);
            scoreEntity.setUserId(record.getUserId());
            scoreEntity.setSessionId(record.getSessionId());
            scoreEntity.setLevel(session.getLevel());
            scoreEntity.setScore(score);
            scoreEntity.setIsPass(isPass);
            scoreEntity.setCreateTime(new Date());
            scoreMapper.insert(scoreEntity);
        }
        return Result.ok();
    }

    private boolean canManageUser(Long userId) {
        SysUser target = userMapper.selectById(userId);
        LoginUser me = UserContext.get();
        return target != null && ScopeUtil.canAccessDept(me, target.getDeptId());
    }
}
