package com.shrescue.business.controller;

import com.alibaba.excel.EasyExcel;
import com.shrescue.business.dto.QuestionExcel;
import com.shrescue.business.dto.TrainProjectExcel;
import com.shrescue.business.entity.QuestionBank;
import com.shrescue.business.entity.TeamTask;
import com.shrescue.business.entity.TrainProject;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.mapper.TeamTaskMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.core.Result;
import com.shrescue.framework.aspect.OpLog;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 批量数据导入接口（JSON + Excel）
 */
@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired
    private TrainProjectMapper projectMapper;
    @Autowired
    private QuestionBankMapper questionMapper;
    @Autowired
    private TeamTaskMapper taskMapper;

    // ===== JSON 导入 =====

    @PostMapping("/train-project")
    public Result<Integer> importTrainProject(@RequestBody List<TrainProject> list) {
        int count = 0;
        for (TrainProject p : list) {
            p.setId(null);
            if (p.getStatus() == null) {
                p.setStatus(1);
            }
            if (p.getSort() == null) {
                p.setSort(0);
            }
            p.setCreateBy(UserContext.getUserId());
            p.setCreateTime(new Date());
            p.setUpdateTime(new Date());
            projectMapper.insert(p);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }

    @PostMapping("/question")
    public Result<Integer> importQuestion(@RequestBody List<QuestionBank> list) {
        int count = 0;
        for (QuestionBank q : list) {
            q.setId(null);
            if (q.getStatus() == null) {
                q.setStatus(1);
            }
            if (q.getScore() == null) {
                q.setScore(BigDecimal.ONE);
            }
            q.setCreateBy(UserContext.getUserId());
            q.setCreateTime(new Date());
            q.setUpdateTime(new Date());
            questionMapper.insert(q);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }

    // ===== Excel 导入 =====

    @OpLog(module = "批量导入", action = "Excel导入训练项目")
    @PostMapping("/train-project/excel")
    public Result<Integer> importTrainProjectExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<TrainProjectExcel> list = EasyExcel.read(file.getInputStream())
                .head(TrainProjectExcel.class).sheet().doReadSync();
        int count = 0;
        for (TrainProjectExcel e : list) {
            if (e.getName() == null || e.getName().isEmpty()) {
                continue;
            }
            TrainProject p = new TrainProject();
            p.setLevel(e.getLevel() == null ? 1 : e.getLevel());
            p.setCategory(e.getCategory());
            p.setName(e.getName());
            p.setOutline(e.getOutline());
            p.setSteps(e.getSteps());
            p.setStandard(e.getStandard());
            p.setTips(e.getTips());
            p.setSort(e.getSort() == null ? 0 : e.getSort());
            p.setStatus(1);
            p.setCreateBy(UserContext.getUserId());
            p.setCreateTime(new Date());
            p.setUpdateTime(new Date());
            projectMapper.insert(p);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }

    @OpLog(module = "批量导入", action = "Excel导入题库")
    @PostMapping("/question/excel")
    public Result<Integer> importQuestionExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<QuestionExcel> list = EasyExcel.read(file.getInputStream())
                .head(QuestionExcel.class).sheet().doReadSync();
        int count = 0;
        for (QuestionExcel e : list) {
            if (e.getContent() == null || e.getContent().isEmpty()) {
                continue;
            }
            QuestionBank q = new QuestionBank();
            q.setLevel(e.getLevel() == null ? 1 : e.getLevel());
            q.setChapter(e.getChapter());
            q.setQuestionType(e.getQuestionType() == null ? 1 : e.getQuestionType());
            q.setContent(e.getContent());
            q.setOptions(e.getOptions());
            q.setAnswer(e.getAnswer());
            q.setAnalysis(e.getAnalysis());
            q.setScore(e.getScore() == null ? BigDecimal.ONE : e.getScore());
            q.setStatus(1);
            q.setCreateBy(UserContext.getUserId());
            q.setCreateTime(new Date());
            q.setUpdateTime(new Date());
            questionMapper.insert(q);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }

    @OpLog(module = "批量导入", action = "导入团队演练方案")
    @PostMapping("/team-task")
    public Result<Integer> importTeamTask(@RequestBody List<TeamTask> list) {
        int count = 0;
        for (TeamTask t : list) {
            t.setId(null);
            if (t.getStatus() == null) {
                t.setStatus(0);
            }
            if (t.getTaskType() == null) {
                t.setTaskType(2);
            }
            t.setIssuerId(UserContext.getUserId());
            t.setCreateBy(UserContext.getUserId());
            t.setCreateTime(new Date());
            t.setUpdateTime(new Date());
            taskMapper.insert(t);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }
}
