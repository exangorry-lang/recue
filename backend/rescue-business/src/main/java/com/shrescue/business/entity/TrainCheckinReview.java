package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 打卡审核记录表
 */
@Data
@TableName("train_checkin_review")
public class TrainCheckinReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkinId;
    private Long reviewerId;
    /** 1=初审 2=终审 */
    private Integer reviewLevel;
    /** 1=通过 2=驳回 */
    private Integer result;
    private String comment;
    private Date reviewTime;
}
