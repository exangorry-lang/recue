package com.shrescue.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrescue.common.constant.Constants;
import com.shrescue.common.exception.BusinessException;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.ScopeUtil;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.entity.SysUserRole;
import com.shrescue.system.mapper.SysUserMapper;
import com.shrescue.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 用户管理服务（含数据范围隔离）
 */
@Service
public class SysUserService {

    private static final long DEFAULT_ROLE_RESCUER = 3L;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Page<SysUser> page(long page, long size, String keyword) {
        LoginUser me = UserContext.get();
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        applyDataScope(qw, me);
        qw.orderByAsc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), qw);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    /** 返回全部用户（供前端 ID→姓名映射，已脱敏密码） */
    public java.util.List<SysUser> listAll() {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId);
        applyDataScope(qw, UserContext.get());
        java.util.List<SysUser> users = userMapper.selectList(qw);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        assertCanManage(user);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public void create(SysUser user) {
        LoginUser me = UserContext.get();
        if (ScopeUtil.hasDeptScope(me)) {
            if (user.getDeptId() == null) {
                user.setDeptId(me.getDeptId());
            }
            if (!ScopeUtil.canAccessDept(me, user.getDeptId())) {
                throw new BusinessException("不能在其他部门创建账号");
            }
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (!StringUtils.hasText(user.getPassword()) || user.getPassword().length() < 8) {
            throw new BusinessException("新用户必须设置至少8位的初始密码");
        }
        String raw = user.getPassword();
        user.setId(null);
        user.setPassword(encoder.encode(raw));
        if (user.getRescueLevel() == null) {
            user.setRescueLevel(1);
        }
        if (user.getJobStatus() == null) {
            user.setJobStatus(1);
        }
        if (user.getEnabled() == null) {
            user.setEnabled(1);
        }
        user.setCreateBy(UserContext.getUserId());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        // 默认绑定救生员角色
        bindRole(user.getId(), DEFAULT_ROLE_RESCUER);
    }

    public void update(SysUser user) {
        SysUser db = userMapper.selectById(user.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        assertCanManage(db);
        LoginUser me = UserContext.get();
        if (ScopeUtil.hasDeptScope(me)) {
            // 部门负责人不能借由编辑把队员迁出本部门。
            user.setDeptId(me.getDeptId());
        }
        if (user.getRescueLevel() != null && (user.getRescueLevel() < 1 || user.getRescueLevel() > 5)) {
            throw new BusinessException("救生员等级应在1至5之间");
        }
        if (user.getEnabled() != null && user.getEnabled() != 0 && user.getEnabled() != 1) {
            throw new BusinessException("账号状态非法");
        }
        if (user.getJobStatus() != null && user.getJobStatus() != 1 && user.getJobStatus() != 2) {
            throw new BusinessException("在岗状态非法");
        }
        // 不允许修改用户名与密码
        user.setUsername(null);
        user.setPassword(null);
        user.setUpdateBy(UserContext.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        checkSelf(id);
        assertCanManage(userMapper.selectById(id));
        userMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer enabled) {
        checkSelf(id);
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException("账号状态非法");
        }
        assertCanManage(userMapper.selectById(id));
        SysUser user = new SysUser();
        user.setId(id);
        user.setEnabled(enabled);
        user.setUpdateBy(UserContext.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    public void updateLevel(Long id, Integer level) {
        checkSelf(id);
        if (level == null || level < 1 || level > 5) {
            throw new BusinessException("救生员等级应在1至5之间");
        }
        assertCanManage(userMapper.selectById(id));
        SysUser user = new SysUser();
        user.setId(id);
        user.setRescueLevel(level);
        user.setUpdateBy(UserContext.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        checkSelf(id);
        assertCanManage(userMapper.selectById(id));
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new BusinessException("临时密码长度不能少于8位");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(encoder.encode(newPassword));
        user.setUpdateBy(UserContext.getUserId());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

    public void bindRole(Long userId, Long roleId) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setCreateTime(new Date());
        userRoleMapper.insert(ur);
    }

    private void applyDataScope(LambdaQueryWrapper<SysUser> qw, LoginUser me) {
        if (me == null || me.getDataScope() == null) {
            return;
        }
        if (me.getDataScope() == Constants.DATA_SCOPE_DEPT) {
            qw.eq(SysUser::getDeptId, me.getDeptId());
        } else if (me.getDataScope() == Constants.DATA_SCOPE_SELF) {
            qw.eq(SysUser::getId, me.getUserId());
        }
        // DATA_SCOPE_ALL 不限制
    }

    private void checkSelf(Long id) {
        if (UserContext.getUserId() != null && UserContext.getUserId().equals(id)) {
            throw new BusinessException("不能操作当前登录账号");
        }
    }

    private void assertCanManage(SysUser target) {
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        LoginUser me = UserContext.get();
        if (!ScopeUtil.canAccessDept(me, target.getDeptId())) {
            throw new BusinessException("无权操作其他部门的账号");
        }
    }
}
