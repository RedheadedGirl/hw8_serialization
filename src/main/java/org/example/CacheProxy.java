package org.example;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheProxy implements InvocationHandler {

    private final Map<String, List<String>> resultByArg = new HashMap<>();
    private Object target;

    public CacheProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(org.example.interfaces.Cache.class)) {

            String keyForMap = createKeyForMap(args);
            if (!resultByArg.containsKey(keyForMap)) {
                System.out.println("key not found, new invocation");
                List<String> result = (List<String>) method.invoke(target, args);
                resultByArg.put(keyForMap, result);
            }
            return resultByArg.get(keyForMap);
        } else {
            return method.invoke(target, args);
        }
    }

    /**
     * Наша мапа в качестве ключа хранит аргументы, приведенные к toString
     */
    private String createKeyForMap(Object[] args) {
        String keyForMap = "";
        for (Object arg: args) {
            keyForMap += arg.toString();
        }
        return keyForMap;
    }
}
