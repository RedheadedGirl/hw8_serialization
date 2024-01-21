package org.example;

import org.example.interfaces.WorkService;
import org.example.service.WorkServiceImpl;
import org.example.utils.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheProxyTest {

    private WorkService cacheProxy;
    private final String fileName = "src/test/resources/saved.bin";

    @BeforeEach
    void setUp() {
        ClassLoader cl = WorkServiceImpl.class.getClassLoader();
        cacheProxy = (WorkService) Proxy.newProxyInstance(cl, new Class[] {WorkService.class},
                new CacheProxy(new WorkServiceImpl(), fileName));
    }

    @AfterEach
    void cleanFile() throws IOException {
        new FileOutputStream(fileName).close();
    }

    @Test
    void givenThreeCalls_whenSomeArgsShouldBeIgnored_ThenIgnoreStringAndDoubleAndSerializeInFile() {
        System.out.println(cacheProxy.run("sd", 1d, new Date(2025, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sd", 2d, new Date(2026, Calendar.JANUARY, 21)));
        System.out.println(cacheProxy.run("sdd", 3d, new Date(2026, Calendar.JANUARY, 21)));

        HashMap<String, Object> deserialize = SerializationUtil.deserialize(fileName);
        assertEquals(2, deserialize.size());
    }

    @Test
    void givenFourCalls_whenMapSizeIsTwo_ThenSerializeInMapOnlyTwo() {
        System.out.println(cacheProxy.work("sd1"));
        System.out.println(cacheProxy.work("sd2"));
        System.out.println(cacheProxy.work("sd2"));
        System.out.println(cacheProxy.work("sd3"));
    }

    @Test
    void givenFourCalls_whenIgnoreDouble_ThenSerializeInFileOnlyTwo() {
        System.out.println(cacheProxy.multiplyOnTwo(3, 17d));
        System.out.println(cacheProxy.multiplyOnTwo(3, 15d));
        System.out.println(cacheProxy.multiplyOnTwo(3, 17d));
        System.out.println(cacheProxy.multiplyOnTwo(4, 17d));

        HashMap<String, Object> deserialize = SerializationUtil.deserialize(fileName);
        assertEquals(2, deserialize.size());
        List<Object> values = deserialize.values().stream().toList();
        assertTrue(values.stream().anyMatch(val -> (double) val == 6));
        assertTrue(values.stream().anyMatch(val -> (double) val == 8));
    }
}