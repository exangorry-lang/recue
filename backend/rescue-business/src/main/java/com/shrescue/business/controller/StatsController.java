package com.shrescue.business.controller;

import com.shrescue.business.entity.TrainCheckin;
import com.shrescue.business.mapper.ExamScoreMapper;
import com.shrescue.business.mapper.QuestionBankMapper;
import com.shrescue.business.mapper.TeamGroupMapper;
import com.shrescue.business.mapper.TrainCheckinMapper;
import com.shrescue.business.mapper.TrainProjectMapper;
import com.shrescue.common.core.Result;
import com.shrescue.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("trainProjectCount", projectMapper.selectCount(null));
        map.put("checkinCount", checkinMapper.selectCount(null));
        map.put("questionCount", questionMapper.selectCount(null));
        map.put("examScoreCount", scoreMapper.selectCount(null));
        map.put("teamGroupCount", groupMapper.selectCount(null));
        map.put("userCount", userMapper.selectCount(null));
        return Result.ok(map);
    }

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
}
