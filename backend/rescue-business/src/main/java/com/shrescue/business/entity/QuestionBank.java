package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 理论题库表
 */
@Data
@TableName("question_bank")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 等级 1-5 */
    private Integer level;
    private String chapter;
    /** 1=单选 2=多选 3=判断 4=简答 */
    private Integer questionType;
    private String content;
    /** 选项 JSON */
    private String options;
    private String answer;
    private String analysis;
    private BigDecimal score;
    /** 状态 1=启用 0=停用 */
    private Integer status;
    private String extJson;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
