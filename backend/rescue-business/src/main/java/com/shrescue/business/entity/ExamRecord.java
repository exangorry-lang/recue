package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试记录表
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long userId;
    private BigDecimal score;
    /** 1=合格 0=不合格 */
    private Integer isPass;
    /** 0=进行中 1=已交卷 2=已阅卷 */
    private Integer status;
    private Date submitTime;
    private Long reviewBy;
    private Date reviewTime;
    private Date createTime;
    private Date updateTime;
}
