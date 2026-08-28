package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 等级晋升台账表
 */
@Data
@TableName("promotion_record")
public class PromotionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer fromLevel;
    private Integer toLevel;
    private String reason;
    /** 0=待终审 1=已通过 2=已驳回 */
    private Integer status;
    private Long applyBy;
    private Date applyTime;
    private Long approveBy;
    private Date approveTime;
    private Date createTime;
    private Date updateTime;
}
