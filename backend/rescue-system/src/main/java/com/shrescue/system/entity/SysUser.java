package com.shrescue.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deptId;
    private String username;
    private String password;
    private String realName;
    private String phone;
    /** 救生员等级 1-5 */
    private Integer rescueLevel;
    /** 在岗状态 1=在岗 2=离岗 */
    private Integer jobStatus;
    /** 账号状态 1=启用 0=禁用 */
    private Integer enabled;
    private Date lastLoginTime;
    private String lastLoginIp;
    private String remark;
    private String extJson;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
