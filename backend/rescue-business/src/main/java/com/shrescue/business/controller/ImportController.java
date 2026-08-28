package com.shrescue.business.controller;

import com.shrescue.business.entity.QuestionBank;
import com.shrescue.business.entity.TrainProject;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 批量数据导入接口（训练项目、题库）
 * 说明：一期以 JSON 批量导入为主；Excel 模板导入后续叠加 Apache POI 实现。
 */
@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired
    private TrainProjectMapper projectMapper;
    @Autowired
    private QuestionBankMapper questionMapper;

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
                q.setScore(java.math.BigDecimal.ONE);
            }
            q.setCreateBy(UserContext.getUserId());
            q.setCreateTime(new Date());
            q.setUpdateTime(new Date());
            questionMapper.insert(q);
            count++;
        }
        return Result.ok("成功导入 " + count + " 条", count);
    }
}
