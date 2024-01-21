package org.example.utils;

import org.example.exceptions.SettingsException;
import org.example.interfaces.Cache;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapUtil {

    public static boolean checkMapHasFreeSpace(Method method, Map<String, List<String>> map) {
        if (method.getReturnType().isAssignableFrom(List.class)) {
            int enoughStoreAmount = method.getAnnotation(Cache.class).enoughStoreAmount();
            if (enoughStoreAmount < 0) {
                throw new SettingsException("Неверно указан размер хранилища!");
            }
            return enoughStoreAmount > map.size();
        }
        return true;
    }

    /**
     * Наша мапа в качестве ключа хранит аргументы, приведенные к toString.
     * ignoreFields - типы полей, которые игнорируются при создании ключа
     */
    public static String createKeyForMap(Object[] args, Method method) {
        Class[] ignore = method.getAnnotation(Cache.class).ignoreFields();
        String prefix = method.getAnnotation(Cache.class).prefix();
        String keyForMap = method.getName();
        if (!prefix.isEmpty()) {
            keyForMap = prefix + method.getName(); // использую префикс как добавку к названию метода если он указан
        }
        if (ignore.length == 0) {
            for (Object arg : args) {
                keyForMap += arg.toString();
            }
        } else {
            for (Object arg : args) {
                boolean argToBeIgnored = Arrays.stream(ignore).anyMatch(ignoringClass -> arg.getClass().isAssignableFrom(ignoringClass));
                if (!argToBeIgnored) {
                    keyForMap += arg.toString();
                }
            }
        }
        return keyForMap;
    }




}
