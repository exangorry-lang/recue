package com.shrescue.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.business.entity.QuestionBank;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.util.LevelAuth;
import com.shrescue.common.core.Result;
import com.shrescue.framework.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题库接口（章节练习、专项刷题、错题复盘、随机自测的数据源）
 */
@RestController
@RequestMapping("/exam/question")
public class QuestionBankController {

    @Autowired
    private QuestionBankMapper questionMapper;

    @GetMapping("/page")
    public Result<Page<QuestionBank>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) Integer level,
                                           @RequestParam(required = false) Integer questionType,
                                           @RequestParam(required = false) String chapter) {
        LambdaQueryWrapper<QuestionBank> qw = new LambdaQueryWrapper<>();
        qw.eq(QuestionBank::getStatus, 1);
        if (level != null) {
            qw.eq(QuestionBank::getLevel, level);
        }
        if (questionType != null) {
            qw.eq(QuestionBank::getQuestionType, questionType);
        }
        if (StringUtils.hasText(chapter)) {
            qw.like(QuestionBank::getChapter, chapter);
        }
        // 等级鉴权
        if (LevelAuth.isRescuer(UserContext.get())) {
            qw.le(QuestionBank::getLevel, UserContext.getLevel());
        }
        qw.orderByAsc(QuestionBank::getLevel).orderByAsc(QuestionBank::getId);
        return Result.ok(questionMapper.selectPage(new Page<>(page, size), qw));
    }
}
