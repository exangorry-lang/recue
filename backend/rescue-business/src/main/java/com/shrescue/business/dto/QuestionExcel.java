package com.shrescue.business.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 题库 Excel 导入模型
 */
@Data
public class QuestionExcel {

    @ExcelProperty("等级")
    private Integer level;

    @ExcelProperty("章节")
    private String chapter;

    @ExcelProperty("题型(1单选2多选3判断4简答)")
    private Integer questionType;

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("选项")
    private String options;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String analysis;

    @ExcelProperty("分值")
    private BigDecimal score;
}
