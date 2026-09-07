package com.shrescue.business.controller;

import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.entity.ExamScore;
import com.shrescue.business.entity.TeamGroup;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.mapper.TeamGroupMapper;
import com.shrescue.business.mapper.TrainCheckinMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.core.Result;
import com.shrescue.common.constant.Constants;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.RequireRole;
import com.shrescue.framework.security.ScopeUtil;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysDept;
import com.shrescue.system.entity.SysUser;
import com.shrescue.system.mapper.SysDeptMapper;
import com.shrescue.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练数据统计与报表导出接口
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private TrainProjectMapper projectMapper;
    @Autowired
    private TrainCheckinMapper checkinMapper;
    @Autowired
    private QuestionBankMapper questionMapper;
    @Autowired
    private ExamScoreMapper scoreMapper;
    @Autowired
    private TeamGroupMapper groupMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysDeptMapper deptMapper;

    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> map = new HashMap<>();
        List<SysUser> scopedUsers = scopedUsers();
        List<Long> userIds = scopedUsers.stream().map(SysUser::getId).toList();
        map.put("trainProjectCount", projectMapper.selectCount(null));
        map.put("checkinCount", userIds.isEmpty() ? 0 : checkinMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TrainCheckin>()
                        .in(TrainCheckin::getUserId, userIds)));
        map.put("questionCount", questionMapper.selectCount(null));
        map.put("examScoreCount", userIds.isEmpty() ? 0 : scoreMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamScore>()
                        .in(ExamScore::getUserId, userIds)));
        map.put("teamGroupCount", ScopeUtil.hasAllScope(UserContext.get()) ? groupMapper.selectCount(null)
                : groupMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamGroup>()
                .eq(TeamGroup::getDeptId, UserContext.getDeptId())));
        map.put("userCount", scopedUsers.size());
        return Result.ok(map);
    }

    @RequireRole(Constants.ROLE_SUPER_ADMIN)
    @GetMapping("/train/export")
    public void exportTrain(HttpServletResponse response) throws IOException {
        List<TrainCheckin> list = checkinMapper.selectList(null);
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("打卡ID,队员ID,训练项目ID,打卡时间,自评,进度,审核状态,离线标记\n");
        for (TrainCheckin c : list) {
            sb.append(c.getId()).append(',')
                    .append(c.getUserId()).append(',')
                    .append(c.getProjectId()).append(',')
                    .append(c.getCheckinTime()).append(',')
                    .append(c.getSelfEval()).append(',')
                    .append(c.getProgress()).append(',')
                    .append(c.getStatus()).append(',')
                    .append(c.getOfflineFlag()).append('\n');
        }
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=train_checkin.csv");
        response.getWriter().write(sb.toString());
    }

    /** 班组维度统计报表 */
    @RequireRole({Constants.ROLE_SUPER_ADMIN, Constants.ROLE_DEPT_LEADER})
    @GetMapping("/dept")
    public Result<List<Map<String, Object>>> deptReport() {
        List<SysDept> depts = deptMapper.selectList(null);
        List<SysUser> users = scopedUsers();
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<TrainCheckin> checkins = userIds.isEmpty() ? List.of() : checkinMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TrainCheckin>()
                        .in(TrainCheckin::getUserId, userIds));
        Map<Long, String> deptName = new HashMap<>();
        for (SysDept d : depts) {
            deptName.put(d.getId(), d.getDeptName());
        }
        Map<Long, Long> userCount = new HashMap<>();
        Map<Long, Long> userDept = new HashMap<>();
        for (SysUser u : users) {
            Long deptId = u.getDeptId() == null ? 0L : u.getDeptId();
            userCount.merge(deptId, 1L, Long::sum);
            userDept.put(u.getId(), deptId);
        }
        Map<Long, Long> checkinCount = new HashMap<>();
        for (TrainCheckin c : checkins) {
            Long deptId = userDept.getOrDefault(c.getUserId(), 0L);
            checkinCount.merge(deptId, 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> e : userCount.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("deptId", e.getKey());
            m.put("deptName", deptName.getOrDefault(e.getKey(), "未分配"));
            m.put("userCount", e.getValue());
            m.put("checkinCount", checkinCount.getOrDefault(e.getKey(), 0L));
            result.add(m);
        }
        return Result.ok(result);
    }

    private List<SysUser> scopedUsers() {
        LoginUser me = UserContext.get();
        if (ScopeUtil.hasAllScope(me)) {
            return userMapper.selectList(null);
        }
        if (ScopeUtil.hasDeptScope(me) && me.getDeptId() != null) {
            return userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getDeptId, me.getDeptId()));
        }
        return List.of();
    }
}
