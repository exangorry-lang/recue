package com.shrescue.framework.security;

import com.shrescue.common.constant.Constants;

/** Small, dependency-free helpers for data-scope checks. */
public final class ScopeUtil {

    private ScopeUtil() { }

    public static boolean hasAllScope(LoginUser user) {
        return user != null && Integer.valueOf(Constants.DATA_SCOPE_ALL).equals(user.getDataScope());
    }

    public static boolean hasDeptScope(LoginUser user) {
        return user != null && Integer.valueOf(Constants.DATA_SCOPE_DEPT).equals(user.getDataScope());
    }

    public static boolean canAccessDept(LoginUser user, Long targetDeptId) {
        return hasAllScope(user) || (hasDeptScope(user) && user.getDeptId() != null
                && user.getDeptId().equals(targetDeptId));
    }
}
