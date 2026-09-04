package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 团队演练复盘/问题台账表
 */
@Data
@TableName("team_review")
public class TeamReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long reviewerId;
    private String summary;
    private String problems;
    private String improvements;
    private Date reviewTime;
    private Date createTime;
    private Date updateTime;
}
