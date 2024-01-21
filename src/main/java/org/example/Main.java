package org.example;

import org.example.interfaces.WorkService;
import org.example.service.WorkServiceImpl;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        WorkServiceImpl workServiceImpl = new WorkServiceImpl();
        ClassLoader cl = WorkServiceImpl.class.getClassLoader();

        WorkService cacheProxy = (WorkService) Proxy.newProxyInstance(cl,
                new Class[] {WorkService.class}, new CacheProxy(workServiceImpl));

        System.out.println(cacheProxy.run("sd", 1d, new Date(2024, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 2d, new Date(2024, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 1d, new Date(2024, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 3d, new Date(2024, Calendar.JANUARY, 21)));
    }
}