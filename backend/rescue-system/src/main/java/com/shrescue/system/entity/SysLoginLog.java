package com.shrescue.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 登录日志表
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String loginIp;
    /** 终端 1=安卓APP 2=PC后台 */
    private Integer deviceType;
    private Date loginTime;
    /** 结果 1=成功 0=失败 */
    private Integer status;
    private String msg;
}
