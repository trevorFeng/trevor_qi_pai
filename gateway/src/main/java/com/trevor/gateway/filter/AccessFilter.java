package com.trevor.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.trevor.common.bo.ResponseHelper;
import com.trevor.common.bo.WebKeys;
import com.trevor.common.domain.mysql.User;
import com.trevor.common.enums.MessageCodeEnum;
import com.trevor.common.service.UserService;
import com.trevor.common.util.JsonUtil;
import com.trevor.common.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class AccessFilter extends ZuulFilter {


    @Resource
    private UserService userService;



    private static final String REDIRECT = "www.knave.top/wechat/";

    /**
     * 前置过滤器
     * pre：可以在请求被路由之前调用
     * route：在路由请求时候被调用,将请求路由到对应的微服务，用于构建发送给微服务的请求
     * post：在route和error过滤器之后被调用,可用来为Response添加HTTP Header、将微服务的Response发送给客户端
     * error：处理请求时发生错误时被调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 优先级为0，数字越大，优先级越低
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 是否执行该过滤器，此处为true，说明需要过滤
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth")) {
            return false;
        }else {
            return true;
        }
    }

    @Override
    public Object run(){
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = request.getHeader(WebKeys.TOKEN);
        if (token == null) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody(JsonUtil.toJsonString(ResponseHelper.createInstanceWithOutData(MessageCodeEnum.TOKEN_ERROR)));
            return null;
        }else {
            //解析token
            Map<String, Object> claims = TokenUtil.getClaimsFromToken(token);
            String openid = (String) claims.get("openid");
            String userId = (String) claims.get("userid");
            Long timestamp = (Long) claims.get("timestamp");

            //三者必须存在,少一样说明token被篡改
            if (openid == null || userId == null || timestamp == null) {
                // 过滤该请求，不对其进行路由
                ctx.setSendZuulResponse(false);
                // 返回错误码
                ctx.setResponseStatusCode(401);
                // 返回错误内容
                ctx.setResponseBody(JsonUtil.toJsonString(ResponseHelper.createInstanceWithOutData(MessageCodeEnum.TOKEN_ERROR)));
                return null;
            }
            return true;

        }
    }
}
