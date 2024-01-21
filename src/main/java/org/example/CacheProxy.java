package org.example;

import org.example.enums.StoreType;
import org.example.utils.MapUtil;
import org.example.utils.SerializationUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheProxy implements InvocationHandler {

    private Map<String, List<String>> resultByArg = new HashMap<>();
    private Object target;
    private String serializationFileName;

    public CacheProxy(Object target, String serializationFileName) {
        this.target = target;
        this.serializationFileName = serializationFileName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(org.example.interfaces.Cache.class)) {
            if (method.getAnnotation(org.example.interfaces.Cache.class).cacheType() == StoreType.IN_MEMORY) {
                return findResultInMemoryOrInvoke(method, args);
            } else {
                HashMap<String, List<String>> deserialized = SerializationUtil.deserialize(serializationFileName); // попробуем достать результат из файла
                String keyForMap = MapUtil.createKeyForMap(args, method);
                if (deserialized.containsKey(keyForMap)) { // если в файле нашли такой ключ
                    return deserialized.get(keyForMap);
                } else { // иначе вызовем метод запишем результат в файл
                    System.out.println("key not found in file, new invocation");
                    List<String> result = (List<String>) method.invoke(target, args);
                    if (!MapUtil.checkMapHasFreeSpace(method, deserialized)) {
                        System.out.println("Хранилище заполнено, новые данные не закэшированы");
                        return result;
                    }
                    deserialized.put(MapUtil.createKeyForMap(args, method), result);
                    SerializationUtil.serialize(deserialized, serializationFileName);
                    return result;
                }
            }
        } else {
            return method.invoke(target, args);
        }
    }

    private Object findResultInMemoryOrInvoke(Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException {
        String keyForMap = MapUtil.createKeyForMap(args, method);
        if (!resultByArg.containsKey(keyForMap)) {
            System.out.println("key not found in memory, new invocation");
            List<String> result = (List<String>) method.invoke(target, args);
            if (!MapUtil.checkMapHasFreeSpace(method, resultByArg)) {
                System.out.println("Хранилище заполнено, новые данные не закэшированы");
                return result;
            }
            resultByArg.put(keyForMap, result);
        }
        return resultByArg.get(keyForMap);
    }
}
