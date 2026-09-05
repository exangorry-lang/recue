package com.shrescue.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.business.dto.AnswerItem;
import com.shrescue.business.entity.ExamAnswer;
import com.shrescue.business.entity.ExamQuestion;
import com.shrescue.business.entity.ExamRecord;
import com.shrescue.business.entity.ExamScore;
import com.shrescue.business.entity.ExamSession;
import com.shrescue.business.entity.QuestionBank;
import com.shrescue.business.mapper.ExamAnswerMapper;
import com.shrescue.business.mapper.ExamQuestionMapper;
import com.shrescue.business.mapper.ExamRecordMapper;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.ExamSessionMapper;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.util.LevelAuth;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试服务（组卷、判分、归档）
 */
@Service
public class ExamService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    @Autowired
    private ExamSessionMapper sessionMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private ExamRecordMapper recordMapper;
    @Autowired
    private ExamAnswerMapper answerMapper;
    @Autowired
    private ExamScoreMapper scoreMapper;
    @Autowired
    private QuestionBankMapper questionMapper;

    /**
     * 创建考试场次并随机组卷
     */
    public void createSession(ExamSession session) {
        session.setId(null);
        session.setStatus(0);
        session.setCreateBy(UserContext.getUserId());
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        sessionMapper.insert(session);
        buildPaper(session.getId(), session.getLevel());
    }

    /**
     * 开始考试（返回题目，不含答案）
     */
    public Map<String, Object> start(Long sessionId) {
        ExamSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("考试场次不存在");
        }
        if (LevelAuth.isRescuer(UserContext.get()) && session.getLevel() > UserContext.getLevel()) {
            throw new BusinessException("等级不足，无法参加该考试");
        }
        Long count = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getSessionId, sessionId)
                .eq(ExamRecord::getUserId, UserContext.getUserId()));
        if (count != null && count > 0) {
            throw new BusinessException("已参加过该考试");
        }
        // 若场次尚未组卷（例如直接入库的测试数据），自动随机组卷
        Long qCount = examQuestionMapper.selectCount(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getSessionId, sessionId));
        if (qCount == null || qCount == 0) {
            buildPaper(sessionId, session.getLevel());
        }
        ExamRecord record = new ExamRecord();
        record.setSessionId(sessionId);
        record.setUserId(UserContext.getUserId());
        record.setStatus(0);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        recordMapper.insert(record);

        List<Map<String, Object>> questions = loadPaper(sessionId, false);
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("session", session);
        result.put("questions", questions);
        return result;
    }

    /**
     * 交卷判分
     */
    public Map<String, Object> submit(Long recordId, List<AnswerItem> answers) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!record.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权操作该考试记录");
        }
        if (record.getStatus() != null && record.getStatus() != 0) {
            throw new BusinessException("该考试已交卷");
        }
        ExamSession session = sessionMapper.selectById(record.getSessionId());

        BigDecimal total = BigDecimal.ZERO;
        int correctCount = 0;
        boolean hasSubjective = false;
        if (answers != null) {
            for (AnswerItem item : answers) {
                QuestionBank q = questionMapper.selectById(item.getQuestionId());
                if (q == null) {
                    continue;
                }
                ExamAnswer ea = new ExamAnswer();
                ea.setRecordId(recordId);
                ea.setQuestionId(item.getQuestionId());
                ea.setUserAnswer(item.getAnswer());
                if (q.getQuestionType() != null && q.getQuestionType() == 4) {
                    hasSubjective = true;
                    ea.setIsCorrect(null);
                    ea.setScore(BigDecimal.ZERO);
                } else {
                    boolean correct = isCorrect(q, item.getAnswer());
                    ea.setIsCorrect(correct ? 1 : 0);
                    BigDecimal score = correct ? (q.getScore() == null ? BigDecimal.ONE : q.getScore()) : BigDecimal.ZERO;
                    ea.setScore(score);
                    total = total.add(score);
                    if (correct) {
                        correctCount++;
                    }
                }
                answerMapper.insert(ea);
            }
        }

        BigDecimal passScore = session.getPassScore() == null ? new BigDecimal("60") : session.getPassScore();
        boolean pass = total.compareTo(passScore) >= 0;
        record.setScore(total);
        record.setIsPass(hasSubjective ? null : (pass ? 1 : 0));
        record.setStatus(hasSubjective ? 1 : 2);
        record.setSubmitTime(new Date());
        record.setUpdateTime(new Date());
        recordMapper.updateById(record);

        ExamScore score = new ExamScore();
        score.setRecordId(recordId);
        score.setUserId(record.getUserId());
        score.setSessionId(record.getSessionId());
        score.setLevel(session.getLevel());
        score.setScore(total);
        score.setIsPass(pass ? 1 : 0);
        score.setCreateTime(new Date());
        scoreMapper.insert(score);

        Map<String, Object> result = new HashMap<>();
        result.put("score", total);
        result.put("passScore", passScore);
        result.put("isPass", pass ? 1 : 0);
        result.put("correctCount", correctCount);
        result.put("hasSubjective", hasSubjective);
        return result;
    }

    /**
     * 组卷：从题库随机抽取
     */
    private void buildPaper(Long sessionId, Integer level) {
        List<QuestionBank> questions = questionMapper.selectList(new LambdaQueryWrapper<QuestionBank>()
                .eq(QuestionBank::getLevel, level)
                .eq(QuestionBank::getStatus, 1));
        Collections.shuffle(questions);
        if (questions.size() > DEFAULT_QUESTION_COUNT) {
            questions = questions.subList(0, DEFAULT_QUESTION_COUNT);
        }
        int sort = 1;
        for (QuestionBank q : questions) {
            ExamQuestion eq = new ExamQuestion();
            eq.setSessionId(sessionId);
            eq.setQuestionId(q.getId());
            eq.setSort(sort++);
            examQuestionMapper.insert(eq);
        }
    }

    /**
     * 加载试卷题目
     */
    private List<Map<String, Object>> loadPaper(Long sessionId, boolean withAnswer) {
        List<ExamQuestion> eqs = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getSessionId, sessionId)
                        .orderByAsc(ExamQuestion::getSort));
        if (eqs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = eqs.stream().map(ExamQuestion::getQuestionId).collect(Collectors.toList());
        List<QuestionBank> questions = questionMapper.selectBatchIds(ids);
        Map<Long, QuestionBank> map = questions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, q -> q));
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExamQuestion eq : eqs) {
            QuestionBank q = map.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("questionType", q.getQuestionType());
            item.put("content", q.getContent());
            item.put("options", q.getOptions());
            item.put("score", q.getScore());
            if (withAnswer) {
                item.put("answer", q.getAnswer());
                item.put("analysis", q.getAnalysis());
            }
            list.add(item);
        }
        return list;
    }

    private boolean isCorrect(QuestionBank q, String userAnswer) {
        if (q.getAnswer() == null || userAnswer == null) {
            return false;
        }
        return q.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }
}
