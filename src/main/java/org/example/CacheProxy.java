package org.example;

import org.example.enums.StoreType;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
            if (method.getAnnotation(org.example.interfaces.Cache.class).cacheType() == StoreType.IN_MEMORY) {
                return findResultInMemoryOrInvoke(method, args);
            } else {
                HashMap<String, List<String>> deserialized = deserialize(); // попробуем достать результат из файла
                String keyForMap = createKeyForMap(args);
                if (deserialized.containsKey(keyForMap)) { // если в файле нашли такой ключ
                    return deserialized.get(keyForMap);
                } else { // иначе вызовем метод запишем результат в файл
                    System.out.println("key not found in file, new invocation");
                    List<String> result = (List<String>) method.invoke(target, args);
                    deserialized.put(createKeyForMap(args), result);
                    serialize(deserialized);
                    return result;
                }
            }
        } else {
            return method.invoke(target, args);
        }
    }

    private void serialize(HashMap<String, List<String>> map) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("saved.bin"))) {
            oos.writeObject(map);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, List<String>> deserialize() {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream("saved.bin"))) {
            return (HashMap<String, List<String>>) oos.readObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return new HashMap<>();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Object findResultInMemoryOrInvoke(Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException {
        String keyForMap = createKeyForMap(args);
        if (!resultByArg.containsKey(keyForMap)) {
            System.out.println("key not found in memory, new invocation");
            List<String> result = (List<String>) method.invoke(target, args);
            resultByArg.put(keyForMap, result);
        }
        return resultByArg.get(keyForMap);
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
