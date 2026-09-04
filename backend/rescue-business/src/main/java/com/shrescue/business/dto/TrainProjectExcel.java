package com.shrescue.business.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 训练项目 Excel 导入模型
 */
@Data
public class TrainProjectExcel {

    @ExcelProperty("等级")
    private Integer level;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("项目名称")
    private String name;

    @ExcelProperty("训练大纲")
    private String outline;

    @ExcelProperty("实操步骤")
    private String steps;

    @ExcelProperty("标准要求")
    private String standard;

    @ExcelProperty("易错提示")
    private String tips;

    @ExcelProperty("排序")
    private Integer sort;
}
