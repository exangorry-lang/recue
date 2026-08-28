package com.shrescue.business.util;

import com.shrescue.common.constant.Constants;
import com.shrescue.framework.security.LoginUser;

/**
 * 等级鉴权工具：救生员只能访问本级及以下内容
 */
public final class LevelAuth {

    private LevelAuth() {
    }

    /**
     * 是否普通救生员（非超管、非部门负责人）
     */
    public static boolean isRescuer(LoginUser user) {
        return user == null
                || (!user.hasRole(Constants.ROLE_SUPER_ADMIN) && !user.hasRole(Constants.ROLE_DEPT_LEADER));
    }
}
