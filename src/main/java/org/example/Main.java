package org.example;

import org.example.interfaces.WorkService;
import org.example.service.WorkServiceImpl;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        ClassLoader cl = WorkServiceImpl.class.getClassLoader();
        WorkService cacheProxy = (WorkService) Proxy.newProxyInstance(cl,
                new Class[] {WorkService.class}, new CacheProxy(new WorkServiceImpl(), "saved.bin"));

        System.out.println(cacheProxy.run("sd", 1d, new Date(2025, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 2d, new Date(2026, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 2d, new Date(2026, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 1d, new Date(2027, Calendar.JANUARY, 21)));

        System.out.println(cacheProxy.work("sd1"));
        System.out.println(cacheProxy.work("sd2"));
        System.out.println(cacheProxy.work("sd2"));
        System.out.println(cacheProxy.work("sd3"));
    }
}