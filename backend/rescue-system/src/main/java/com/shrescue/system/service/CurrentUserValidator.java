package com.shrescue.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.ResultCode;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.AuthenticatedUserValidator;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.system.entity.SysRole;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.entity.SysUserRole;
import com.shrescue.system.mapper.SysRoleMapper;
import com.shrescue.system.mapper.SysUserMapper;
import com.shrescue.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Reloads mutable account state on every authenticated request. */
@Component
public class CurrentUserValidator implements AuthenticatedUserValidator {

    @Autowired private SysUserMapper userMapper;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private SysRoleMapper roleMapper;

    @Override
    public LoginUser validate(LoginUser tokenUser) {
        SysUser user = userMapper.selectById(tokenUser.getUserId());
        if (user == null || Integer.valueOf(0).equals(user.getEnabled())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号已禁用或不存在");
        }
        List<String> roles = new ArrayList<>();
        int dataScope = Constants.DATA_SCOPE_SELF;
        List<SysUserRole> mappings = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        for (SysUserRole mapping : mappings) {
            SysRole role = roleMapper.selectById(mapping.getRoleId());
            if (role != null) {
                roles.add(role.getRoleCode());
                if (role.getDataScope() != null && role.getDataScope() < dataScope) {
                    dataScope = role.getDataScope();
                }
            }
        }
        LoginUser refreshed = new LoginUser();
        refreshed.setUserId(user.getId());
        refreshed.setUsername(user.getUsername());
        refreshed.setRealName(user.getRealName());
        refreshed.setLevel(user.getRescueLevel());
        refreshed.setDeptId(user.getDeptId());
        refreshed.setRoles(roles);
        refreshed.setDataScope(dataScope);
        return refreshed;
    }
}
