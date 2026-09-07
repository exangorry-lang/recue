package com.shrescue.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.JwtUtil;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.system.dto.LoginDTO;
import com.shrescue.system.entity.SysLoginLog;
import com.shrescue.system.entity.SysRole;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.entity.SysUserRole;
import com.shrescue.system.mapper.SysLoginLogMapper;
import com.shrescue.system.mapper.SysRoleMapper;
import com.shrescue.system.mapper.SysUserMapper;
import com.shrescue.system.mapper.SysUserRoleMapper;
import com.shrescue.system.vo.LoginVO;
import com.shrescue.system.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 认证服务
 */
@Service
public class AuthService {

    /** 初始密码占位符，首次登录时自动加密为 BCrypt */
    private static final String INIT_PASSWORD_PLACEHOLDER = "__CHANGE_ME_BCRYPT__";
    @Value("${shrescue.bootstrap.initial-password}")
    private String initialPassword;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysLoginLogMapper loginLogMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public LoginVO login(LoginDTO dto, String ip) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            saveLoginLog(null, dto.getUsername(), ip, dto.getDeviceType(), 0, "用户不存在");
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            saveLoginLog(user.getId(), user.getUsername(), ip, dto.getDeviceType(), 0, "账号已禁用");
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 密码校验（兼容初始占位符）
        boolean pwdOk;
        if (INIT_PASSWORD_PLACEHOLDER.equals(user.getPassword())) {
            pwdOk = initialPassword.equals(dto.getPassword());
            if (pwdOk) {
                user.setPassword(encoder.encode(initialPassword));
                userMapper.updateById(user);
            }
        } else {
            pwdOk = encoder.matches(dto.getPassword(), user.getPassword());
        }
        if (!pwdOk) {
            saveLoginLog(user.getId(), user.getUsername(), ip, dto.getDeviceType(), 0, "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 加载角色与数据范围
        List<String> roleCodes = new ArrayList<>();
        int dataScope = Constants.DATA_SCOPE_SELF;
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        for (SysUserRole ur : userRoles) {
            SysRole role = roleMapper.selectById(ur.getRoleId());
            if (role != null) {
                roleCodes.add(role.getRoleCode());
                if (role.getDataScope() != null && role.getDataScope() < dataScope) {
                    dataScope = role.getDataScope();
                }
            }
        }

        // PC 管理后台仅限超级管理员与部门负责人登录
        if (dto.getDeviceType() != null && dto.getDeviceType() == 2
                && !roleCodes.contains(Constants.ROLE_SUPER_ADMIN)
                && !roleCodes.contains(Constants.ROLE_DEPT_LEADER)) {
            saveLoginLog(user.getId(), user.getUsername(), ip, dto.getDeviceType(), 0, "无PC后台权限");
            throw new BusinessException("PC管理后台仅限管理员和部门负责人登录");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setLevel(user.getRescueLevel());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoles(roleCodes);
        loginUser.setDataScope(dataScope);

        String token = jwtUtil.createToken(loginUser);

        // 更新最后登录信息
        user.setLastLoginTime(new Date());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        saveLoginLog(user.getId(), user.getUsername(), ip, dto.getDeviceType(), 1, null);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(buildUserInfo(loginUser));
        return vo;
    }

    public UserInfoVO currentUser() {
        LoginUser u = com.shrescue.framework.security.UserContext.get();
        if (u == null) {
            throw new BusinessException(401, "未登录");
        }
        return buildUserInfo(u);
    }

    public void changePassword(String oldPassword, String newPassword) {
        LoginUser me = com.shrescue.framework.security.UserContext.get();
        if (me == null) {
            throw new BusinessException(401, "未登录");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        SysUser user = userMapper.selectById(me.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        user.setUpdateBy(me.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    private UserInfoVO buildUserInfo(LoginUser u) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(u.getUserId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setLevel(u.getLevel());
        vo.setDeptId(u.getDeptId());
        vo.setRoles(u.getRoles());
        vo.setDataScope(u.getDataScope());
        return vo;
    }

    private void saveLoginLog(Long userId, String username, String ip, Integer deviceType, int status, String msg) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginIp(ip);
        log.setDeviceType(deviceType == null ? 1 : deviceType);
        log.setLoginTime(new Date());
        log.setStatus(status);
        log.setMsg(msg);
        loginLogMapper.insert(log);
    }
}
