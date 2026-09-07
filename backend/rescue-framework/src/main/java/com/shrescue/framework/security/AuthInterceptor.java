package com.shrescue.framework.security;

import com.shrescue.common.constant.Constants;
import com.shrescue.common.core.ResultCode;
import com.shrescue.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器：解析 Token 并写入用户上下文
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /** Implemented by rescue-system; optional so framework stays reusable. */
    @Autowired(required = false)
    private AuthenticatedUserValidator authenticatedUserValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String header = request.getHeader(Constants.TOKEN_HEADER);
        if (header == null || !header.startsWith(Constants.TOKEN_PREFIX)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        String token = header.substring(Constants.TOKEN_PREFIX.length());
        try {
            LoginUser user = jwtUtil.parseToken(token);
            if (authenticatedUserValidator != null) {
                user = authenticatedUserValidator.validate(user);
            }
            UserContext.set(user);

            // 接口角色权限校验
            if (handler instanceof HandlerMethod handlerMethod) {
                RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
                if (requireRole == null) {
                    requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
                }
                if (requireRole != null) {
                    boolean allowed = false;
                    if (user.getRoles() != null) {
                        for (String required : requireRole.value()) {
                            if (user.getRoles().contains(required)) {
                                allowed = true;
                                break;
                            }
                        }
                    }
                    if (!allowed) {
                        throw new BusinessException(ResultCode.FORBIDDEN, "无权限执行该操作");
                    }
                }
            }
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录凭证无效或已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
