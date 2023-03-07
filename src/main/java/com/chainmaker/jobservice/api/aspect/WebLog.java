package com.chainmaker.jobservice.api.aspect;
import java.lang.annotation.*;
/**
 * @author gaokang
 * @date 2022-09-08 17:08
 * @description:接口日志
 * @version: 1.0.0
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface WebLog {
    /***
     * @description 日志描述信息
     * @param
     * @return java.lang.String
     * @author gaokang
     * @date 2022/9/8 20:04
     */
    String description() default "";

}