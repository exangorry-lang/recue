package com.shrescue.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志表
 */
@Data
@TableName("sys_op_log")
public class SysOpLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String method;
    private String requestIp;
    private Date opTime;
    /** 结果 1=成功 0=失败 */
    private Integer status;
    private String detail;
}
