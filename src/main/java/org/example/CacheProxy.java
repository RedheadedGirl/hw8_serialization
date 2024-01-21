package org.example;

import org.example.enums.StoreType;
import org.example.exceptions.SerializationException;
import org.example.exceptions.SettingsException;
import org.example.interfaces.Cache;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheProxy implements InvocationHandler {

    private Map<String, List<String>> resultByArg = new HashMap<>();
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
                String keyForMap = createKeyForMap(args, method);
                if (deserialized.containsKey(keyForMap)) { // если в файле нашли такой ключ
                    return deserialized.get(keyForMap);
                } else { // иначе вызовем метод запишем результат в файл
                    System.out.println("key not found in file, new invocation");
                    List<String> result = (List<String>) method.invoke(target, args);
                    if (!checkMapHasFreeSpace(method, deserialized)) {
                        System.out.println("Хранилище заполнено, новые данные не закэшированы");
                        return result;
                    }
                    deserialized.put(createKeyForMap(args, method), result);
                    serialize(deserialized);
                    return result;
                }
            }
        } else {
            return method.invoke(target, args);
        }
    }

    private boolean checkMapHasFreeSpace(Method method, Map<String, List<String>> map) {
        if (method.getReturnType().isAssignableFrom(List.class)) {
            int enoughStoreAmount = method.getAnnotation(Cache.class).enoughStoreAmount();
            if (enoughStoreAmount < 0) {
                throw new SettingsException("Неверно указан размер хранилища!");
            }
            return enoughStoreAmount > map.size();
        }
        return true;
    }

    private void serialize(HashMap<String, List<String>> map) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("saved.bin"))) {
            oos.writeObject(map);
        } catch (IOException e) {
            throw new SerializationException("Возникла ошибка при сериализации данных в файл");
        }
    }

    private HashMap<String, List<String>> deserialize() {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream("saved.bin"))) {
            return (HashMap<String, List<String>>) oos.readObject();
        } catch (FileNotFoundException e) {
            throw new SerializationException("Не найден файл для десериализации");
        } catch (IOException e) {
            return new HashMap<>();
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Проблемы с кастованием при десериализации");
        }
    }

    private Object findResultInMemoryOrInvoke(Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException {
        String keyForMap = createKeyForMap(args, method);
        if (!resultByArg.containsKey(keyForMap)) {
            System.out.println("key not found in memory, new invocation");
            List<String> result = (List<String>) method.invoke(target, args);
            if (!checkMapHasFreeSpace(method, resultByArg)) {
                System.out.println("Хранилище заполнено, новые данные не закэшированы");
                return result;
            }
            resultByArg.put(keyForMap, result);
        }
        return resultByArg.get(keyForMap);
    }

    /**
     * Наша мапа в качестве ключа хранит аргументы, приведенные к toString.
     * ignoreFields - типы полей, которые игнорируются при создании ключа
     */
    private String createKeyForMap(Object[] args, Method method) {
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
