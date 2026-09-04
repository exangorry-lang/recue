package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.business.entity.ExamAnswer;
import com.shrescue.business.entity.ExamRecord;
import com.shrescue.business.entity.ExamScore;
import com.shrescue.business.entity.QuestionBank;
import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.entity.TrainProject;
import com.shrescue.business.mapper.ExamAnswerMapper;
import com.shrescue.business.mapper.ExamRecordMapper;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.mapper.TrainCheckinMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 个人台账接口（训练进度、能力短板、错题复盘、历次成绩）
 */
@RestController
@RequestMapping("/me")
public class MineController {

    @Autowired
    private TrainCheckinMapper checkinMapper;
    @Autowired
    private TrainProjectMapper projectMapper;
    @Autowired
    private ExamAnswerMapper answerMapper;
    @Autowired
    private ExamRecordMapper recordMapper;
    @Autowired
    private ExamScoreMapper scoreMapper;
    @Autowired
    private QuestionBankMapper questionMapper;

    /** 训练进度台账 */
    @GetMapping("/train/progress")
    public Result<List<Map<String, Object>>> trainProgress() {
        List<TrainCheckin> checkins = checkinMapper.selectList(
                new LambdaQueryWrapper<TrainCheckin>()
                        .eq(TrainCheckin::getUserId, UserContext.getUserId())
                        .orderByDesc(TrainCheckin::getId));
        List<Long> pids = checkins.stream().map(TrainCheckin::getProjectId).distinct().collect(Collectors.toList());
        Map<Long, String> pname = new HashMap<>();
        if (!pids.isEmpty()) {
            for (TrainProject p : projectMapper.selectBatchIds(pids)) {
                pname.put(p.getId(), p.getName());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (TrainCheckin c : checkins) {
            Map<String, Object> m = new HashMap<>();
            m.put("projectName", pname.getOrDefault(c.getProjectId(), "项目" + c.getProjectId()));
            m.put("checkinTime", c.getCheckinTime());
            m.put("selfEval", c.getSelfEval());
            m.put("progress", c.getProgress());
            m.put("status", c.getStatus());
            result.add(m);
        }
        return Result.ok(result);
    }

    /** 能力短板台账 */
    @GetMapping("/train/weak")
    public Result<Map<String, Object>> weakPoints() {
        Map<String, Object> result = new HashMap<>();
        Long weakCheckins = checkinMapper.selectCount(
                new LambdaQueryWrapper<TrainCheckin>()
                        .eq(TrainCheckin::getUserId, UserContext.getUserId())
                        .eq(TrainCheckin::getSelfEval, 3));
        long wrongCount = 0;
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, UserContext.getUserId()));
        if (!records.isEmpty()) {
            List<Long> rids = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
            wrongCount = answerMapper.selectCount(
                    new LambdaQueryWrapper<ExamAnswer>()
                            .in(ExamAnswer::getRecordId, rids)
                            .eq(ExamAnswer::getIsCorrect, 0));
        }
        result.put("weakCheckinCount", weakCheckins == null ? 0 : weakCheckins);
        result.put("wrongQuestionCount", wrongCount);
        return Result.ok(result);
    }

    /** 错题复盘 */
    @GetMapping("/exam/wrong")
    public Result<List<Map<String, Object>>> wrongList() {
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, UserContext.getUserId()));
        if (records.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }
        List<Long> rids = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        List<ExamAnswer> wrongs = answerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .in(ExamAnswer::getRecordId, rids)
                        .eq(ExamAnswer::getIsCorrect, 0));
        if (wrongs.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }
        List<Long> qids = wrongs.stream().map(ExamAnswer::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, QuestionBank> qmap = new HashMap<>();
        for (QuestionBank q : questionMapper.selectBatchIds(qids)) {
            qmap.put(q.getId(), q);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamAnswer w : wrongs) {
            QuestionBank q = qmap.get(w.getQuestionId());
            if (q == null) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("questionId", q.getId());
            m.put("content", q.getContent());
            m.put("options", q.getOptions());
            m.put("questionType", q.getQuestionType());
            m.put("myAnswer", w.getUserAnswer());
            m.put("answer", q.getAnswer());
            m.put("analysis", q.getAnalysis());
            result.add(m);
        }
        return Result.ok(result);
    }

    /** 历次考核成绩 */
    @GetMapping("/exam/history")
    public Result<List<ExamScore>> examHistory() {
        return Result.ok(scoreMapper.selectList(
                new LambdaQueryWrapper<ExamScore>()
                        .eq(ExamScore::getUserId, UserContext.getUserId())
                        .orderByDesc(ExamScore::getId)));
    }
}
