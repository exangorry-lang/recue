package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 团队参与记录表
 */
@Data
@TableName("team_record")
public class TeamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    private Date checkinTime;
    /** 个人贡献评分 */
    private BigDecimal contribution;
    private String scoreDetail;
    private String comment;
    /** 0=未参与 1=已打卡 2=已评分 */
    private Integer status;
    private Integer offlineFlag;
    private Date createTime;
    private Date updateTime;

    /** 队员姓名（非表字段，接口返回时填充） */
    @TableField(exist = false)
    private String userName;
}
