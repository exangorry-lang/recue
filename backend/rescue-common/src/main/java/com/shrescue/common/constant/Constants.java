package com.shrescue.common.constant;

/**
 * 全局常量
 */
public interface Constants {

    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    /** 角色编码 */
    String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    String ROLE_DEPT_LEADER = "DEPT_LEADER";
    String ROLE_RESCUER = "RESCUER";

    /** JWT claim key */
    String CLAIM_USER_ID = "uid";
    String CLAIM_USERNAME = "username";
    String CLAIM_REAL_NAME = "realName";
    String CLAIM_LEVEL = "level";
    String CLAIM_DEPT_ID = "deptId";
    String CLAIM_ROLES = "roles";
    String CLAIM_DATA_SCOPE = "dataScope";

    /** 数据范围 */
    int DATA_SCOPE_ALL = 1;
    int DATA_SCOPE_DEPT = 2;
    int DATA_SCOPE_SELF = 3;
}
