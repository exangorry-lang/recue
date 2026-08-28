package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 训练打卡表
 */
@Data
@TableName("train_checkin")
public class TrainCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private Date checkinTime;
    /** 自评 1=合格 2=基本合格 3=不合格 */
    private Integer selfEval;
    private String selfComment;
    /** 进度 0-100 */
    private Integer progress;
    /** 审核状态 0=待审 1=通过 2=驳回 */
    private Integer status;
    /** 离线标记 0=在线 1=离线补传 */
    private Integer offlineFlag;
    private String extJson;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
