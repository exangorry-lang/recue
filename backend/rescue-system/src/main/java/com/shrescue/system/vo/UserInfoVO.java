package com.shrescue.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前用户信息
 */
@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String realName;
    private Integer level;
    private Long deptId;
    private List<String> roles;
    private Integer dataScope;
}
