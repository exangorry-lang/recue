package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试场次表
 */
@Data
@TableName("exam_session")
public class ExamSession {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 等级 1-5 */
    private Integer level;
    /** 1=模拟自测 2=正式晋升考试 */
    private Integer examType;
    private Date startTime;
    private Date endTime;
    /** 时长（分钟） */
    private Integer duration;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
