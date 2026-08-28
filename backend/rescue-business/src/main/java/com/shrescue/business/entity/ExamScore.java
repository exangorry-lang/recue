package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 成绩归档表
 */
@Data
@TableName("exam_score")
public class ExamScore {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long userId;
    private Long sessionId;
    private Integer level;
    private BigDecimal score;
    /** 1=合格 0=不合格 */
    private Integer isPass;
    private String archiveJson;
    private Date createTime;
}
