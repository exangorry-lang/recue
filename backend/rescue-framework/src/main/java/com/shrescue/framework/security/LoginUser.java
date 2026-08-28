package com.shrescue.framework.security;

import lombok.Data;

import java.util.List;

/**
 * 当前登录用户（Token 载荷与上下文）
 */
@Data
public class LoginUser {

    private Long userId;
    private String username;
    private String realName;
    /** 救生员等级 1-5 */
    private Integer level;
    /** 所属部门/班组ID */
    private Long deptId;
    /** 角色编码列表 */
    private List<String> roles;
    /** 数据范围：1=全部 2=本部门 3=本人 */
    private Integer dataScope;

    public boolean hasRole(String roleCode) {
        return roles != null && roles.contains(roleCode);
    }
}
