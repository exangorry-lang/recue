package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 个人在岗能力档案表
 */
@Data
@TableName("user_archive")
public class UserArchive {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer currentLevel;
    private BigDecimal totalTrainHours;
    private Integer totalCheckin;
    private Integer totalExam;
    private String weakPoints;
    private String archiveJson;
    private Date updateTime;
}
