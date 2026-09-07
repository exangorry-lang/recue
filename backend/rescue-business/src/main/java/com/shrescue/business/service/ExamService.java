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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void createSession(ExamSession session) {
        if (session.getName() == null || session.getName().isBlank()
                || session.getLevel() == null || session.getLevel() < 1 || session.getLevel() > 5) {
            throw new BusinessException("考试名称和等级不合法");
        }
        if (session.getDuration() == null || session.getDuration() <= 0
                || session.getTotalScore() == null || session.getTotalScore().compareTo(BigDecimal.ZERO) <= 0
                || session.getPassScore() == null || session.getPassScore().compareTo(BigDecimal.ZERO) < 0
                || session.getPassScore().compareTo(session.getTotalScore()) > 0) {
            throw new BusinessException("考试时长、总分或及格线不合法");
        }
        session.setId(null);
        // 新建场次默认可用；需要定时开放时由 start/endTime 控制。
        session.setStatus(1);
        session.setCreateBy(UserContext.getUserId());
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        sessionMapper.insert(session);
        buildPaper(session.getId(), session.getLevel());
    }

    /**
     * 开始考试（返回题目，不含答案）
     */
    @Transactional
    public Map<String, Object> start(Long sessionId) {
        ExamSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("考试场次不存在");
        }
        Date now = new Date();
        if (!Integer.valueOf(1).equals(session.getStatus())
                || (session.getStartTime() != null && now.before(session.getStartTime()))
                || (session.getEndTime() != null && now.after(session.getEndTime()))) {
            throw new BusinessException("该考试当前未开放");
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
        if (examQuestionMapper.selectCount(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getSessionId, sessionId)) == 0) {
            throw new BusinessException("该等级暂无可用于考试的题目");
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
    @Transactional
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
        if (session == null) {
            throw new BusinessException("考试场次不存在");
        }
        Date now = new Date();
        if (session.getDuration() != null && record.getCreateTime() != null
                && now.after(new Date(record.getCreateTime().getTime() + session.getDuration() * 60_000L))) {
            throw new BusinessException("考试时间已结束");
        }

        List<ExamQuestion> paper = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getSessionId, record.getSessionId()));
        if (paper.isEmpty()) {
            throw new BusinessException("考试试卷为空");
        }
        List<Long> questionIds = paper.stream().map(ExamQuestion::getQuestionId).collect(Collectors.toList());
        List<QuestionBank> paperQuestions = questionMapper.selectBatchIds(questionIds);
        if (paperQuestions.size() != questionIds.size()) {
            throw new BusinessException("试卷题目已失效，请联系管理员");
        }
        Map<Long, QuestionBank> questionMap = paperQuestions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, q -> q));
        Map<Long, String> submitted = new HashMap<>();
        if (answers != null) {
            for (AnswerItem item : answers) {
                if (item == null || item.getQuestionId() == null || !questionMap.containsKey(item.getQuestionId())) {
                    throw new BusinessException("提交了不属于本场考试的题目");
                }
                if (submitted.containsKey(item.getQuestionId())) {
                    throw new BusinessException("同一题目不能重复提交");
                }
                submitted.put(item.getQuestionId(), item.getAnswer());
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        int correctCount = 0;
        List<Map<String, Object>> questionResults = new ArrayList<>();
        boolean hasSubjective = paperQuestions.stream().anyMatch(q -> Integer.valueOf(4).equals(q.getQuestionType()));
        BigDecimal rawFullScore = paperQuestions.stream()
                .map(q -> q.getScore() == null ? BigDecimal.ONE : q.getScore())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (rawFullScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("试卷分值配置错误");
        }
        BigDecimal totalScore = session.getTotalScore() == null ? rawFullScore : session.getTotalScore();
        for (QuestionBank q : paperQuestions) {
            String answer = submitted.get(q.getId());
            ExamAnswer ea = new ExamAnswer();
            ea.setRecordId(recordId);
            ea.setQuestionId(q.getId());
            ea.setUserAnswer(answer);
            Map<String, Object> qr = new HashMap<>();
            qr.put("questionId", q.getId());
            qr.put("questionType", q.getQuestionType());
            qr.put("content", q.getContent());
            qr.put("options", q.getOptions());
            qr.put("userAnswer", answer);
            qr.put("answer", q.getAnswer());
            qr.put("analysis", q.getAnalysis());
            if (Integer.valueOf(4).equals(q.getQuestionType())) {
                ea.setIsCorrect(null);
                ea.setScore(BigDecimal.ZERO);
                qr.put("isCorrect", null);
            } else {
                boolean correct = isCorrect(q, answer);
                ea.setIsCorrect(correct ? 1 : 0);
                BigDecimal weight = (q.getScore() == null ? BigDecimal.ONE : q.getScore())
                        .multiply(totalScore).divide(rawFullScore, 2, java.math.RoundingMode.HALF_UP);
                BigDecimal score = correct ? weight : BigDecimal.ZERO;
                ea.setScore(score);
                total = total.add(score);
                if (correct) {
                    correctCount++;
                }
                qr.put("isCorrect", correct ? 1 : 0);
            }
            answerMapper.insert(ea);
            questionResults.add(qr);
        }

        BigDecimal passScore = session.getPassScore() == null ? totalScore.multiply(new BigDecimal("0.6")) : session.getPassScore();
        boolean pass = total.compareTo(passScore) >= 0;
        record.setScore(total);
        record.setIsPass(hasSubjective ? null : (pass ? 1 : 0));
        record.setStatus(hasSubjective ? 1 : 2);
        record.setSubmitTime(new Date());
        record.setUpdateTime(new Date());
        recordMapper.updateById(record);

        // 有简答题时需人工阅卷，阅卷后再归档最终成绩
        if (!hasSubjective) {
            ExamScore score = new ExamScore();
            score.setRecordId(recordId);
            score.setUserId(record.getUserId());
            score.setSessionId(record.getSessionId());
            score.setLevel(session.getLevel());
            score.setScore(total);
            score.setIsPass(pass ? 1 : 0);
            score.setCreateTime(new Date());
            scoreMapper.insert(score);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("score", total);
        result.put("passScore", passScore);
        result.put("isPass", hasSubjective ? null : (pass ? 1 : 0));
        result.put("correctCount", correctCount);
        result.put("hasSubjective", hasSubjective);
        result.put("questionResults", questionResults);
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
        if (questions.isEmpty()) {
            throw new BusinessException("该等级暂无可用于考试的题目");
        }
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
        String expected = normalizeAnswer(q.getAnswer());
        String actual = normalizeAnswer(userAnswer);
        return expected.equals(actual);
    }

    private String normalizeAnswer(String answer) {
        String trimmed = answer.trim().toUpperCase().replaceAll("[\\s,，、]", "");
        // Clients normally submit option codes (A / ABC). Also accept a legacy
        // display value such as "A. 面镜" for single-choice compatibility.
        if (trimmed.matches("^[A-Z][.．].*")) {
            trimmed = trimmed.substring(0, 1);
        }
        char[] values = trimmed.toCharArray();
        java.util.Arrays.sort(values);
        return new String(values);
    }
}
