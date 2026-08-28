package com.shrescue.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 训练素材表
 */
@Data
@TableName("train_material")
public class TrainMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    /** 1=图片 2=视频 3=文档 */
    private Integer materialType;
    private String title;
    private String url;
    private Integer sort;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
