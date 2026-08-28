package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 团队/班组表
 */
@Data
@TableName("team_group")
public class TeamGroup {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long deptId;
    private Long leaderId;
    /** 状态 1=启用 0=停用 */
    private Integer status;
    private String remark;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
