package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 团队任务/演练计划表
 */
@Data
@TableName("team_task")
public class TeamTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private String name;
    /** 1=日常实训 2=团队救援演练 */
    private Integer taskType;
    private String content;
    private Date startTime;
    private Date endTime;
    private Long issuerId;
    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;
    private String extJson;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
