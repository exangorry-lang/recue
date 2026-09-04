package com.shrescue.system.aspect;

import com.shrescue.framework.aspect.OpLog;
import com.shrescue.framework.security.LoginUser;
import com.shrescue.framework.security.UserContext;
import com.shrescue.system.entity.SysOpLog;
import com.shrescue.system.mapper.SysOpLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 操作日志切面（全操作留痕可追溯）
 */
@Aspect
@Component
public class OpLogAspect {

    @Autowired
    private SysOpLogMapper opLogMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        int status = 1;
        String error = null;
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            status = 0;
            error = e.getMessage();
            throw e;
        } finally {
            saveLog(pjp, opLog, status, error);
        }
    }

    private void saveLog(ProceedingJoinPoint pjp, OpLog opLog, int status, String error) {
        try {
            LoginUser u = UserContext.get();
            SysOpLog log = new SysOpLog();
            log.setUserId(u == null ? null : u.getUserId());
            log.setUsername(u == null ? null : u.getUsername());
            log.setModule(opLog.module());
            log.setAction(opLog.action());
            log.setMethod(pjp.getSignature().toShortString());
            log.setOpTime(new Date());
            log.setStatus(status);
            log.setDetail(error);
            opLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志记录失败不影响业务
        }
    }
}
