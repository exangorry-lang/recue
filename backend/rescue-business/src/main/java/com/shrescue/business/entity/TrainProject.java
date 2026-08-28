package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 训练项目表
 */
@Data
@TableName("train_project")
public class TrainProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 等级 1-5 */
    private Integer level;
    private String category;
    private String name;
    private String outline;
    private String steps;
    private String standard;
    private String tips;
    private Integer sort;
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
