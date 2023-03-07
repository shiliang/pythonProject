package com.chainmaker.jobservice.api.aspect;

import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.response.ContractException;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.api.response.RestException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author gaokang
 * @date 2022-09-08 17:09
 * @description:接口日志切面
 * @version: 1.0.0
 */

@Aspect
@Component
//@Profile({"dev", "test"})
public class WebLogAspect {
    long startTime;
    private final static Logger logger         = LoggerFactory.getLogger(WebLogAspect.class);
    /** 换行符 */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /** 以自定义 @WebLog 注解为切点 */
    @Pointcut("@annotation(com.chainmaker.jobservice.api.aspect.WebLog)")
    public void webLog() {}

    /***
     * @description 在切点之前执行打印参数日志
     * @param joinPoint
     * @return void
     * @author gaokang
     * @date 2022/9/8 20:02
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime = System.currentTimeMillis();
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取 @WebLog 注解的描述信息
        String methodDescription = getAspectLogDescription(joinPoint);

        // 打印请求相关参数
        logger.info("========================================== Start ==========================================");
        // 打印请求 url
        logger.info("URL            : {}", request.getRequestURL().toString());
        // 打印描述信息
        logger.info("Description    : {}", methodDescription);
        // 打印 Http method
        logger.info("HTTP Method    : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        logger.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        logger.info("IP             : {}", request.getRemoteAddr());
        // 打印请求入参
        logger.info("Request Args   : {}", JSONObject.toJSONString(joinPoint.getArgs()));
    }

    /***
     * @description 在成功返回后打印日志
     * @param joinPoint
     * @param result
     * @return void
     * @author gaokang
     * @date 2022/9/8 20:02
     */
    @AfterReturning(pointcut = "webLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        // 打印出参
        logger.info("Response Args  : {}", JSONObject.toJSONString(result));
        // 执行耗时
        logger.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
        logger.info("=========================================== Success ===========================================" + LINE_SEPARATOR);
    }

    /***
     * @description 抛出异常时打印日志
     * @param e
     * @return void
     * @author gaokang
     * @date 2022/9/8 20:02
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void handleThrowing(Throwable e) {
        if (e instanceof ParserException) {
            logger.error("SQL解析异常！原因是:", e);
        } else if (e instanceof NullPointerException) {
            logger.error("发生空指针异常！原因是:",e);
        } else if (e instanceof ContractException) {
            logger.error("调用合约失败！原因是:",e);
        } else if (e instanceof RestClientException || e instanceof RestException) {
            logger.error("请求后台数据异常！原因是:",e);
        } else {
            logger.error("未知异常！原因是:",e);
        }
        logger.info("=========================================== Failed ===========================================" + LINE_SEPARATOR);
    }

    /***
     * @description 获取接口描述
     * @param joinPoint
     * @return java.lang.String
     * @author gaokang
     * @date 2022/9/8 20:02
     */
    public String getAspectLogDescription(JoinPoint joinPoint)
            throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder("");
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description.append(method.getAnnotation(WebLog.class).description());
                    break;
                }
            }
        }
        return description.toString();
    }

}