package com.zz.db.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangzuizui
 * @date 2018/3/20 9:58
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DoRoute {

    /**
     * 路由字段
     * @return
     */
    String routeFile() default "";

}
