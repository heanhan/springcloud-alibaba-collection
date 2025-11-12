package com.jhzhao.alibaba.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    String key();

    double permitsPerSecond();

    long timeout() default 500;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
