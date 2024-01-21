package org.example.interfaces;

import org.example.enums.StoreType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cache {
    public StoreType cacheType() default StoreType.IN_MEMORY;
    public String prefix() default "";
    public boolean zip() default false;
    public Class[] ignoreFields() default {};
    public int enoughStoreAmount() default 100;
}