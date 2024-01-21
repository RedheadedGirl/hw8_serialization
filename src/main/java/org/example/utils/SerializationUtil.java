package org.example.utils;

import org.example.exceptions.SerializationException;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class SerializationUtil {

    public static void serialize(HashMap<String, List<String>> map, String serializationFileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializationFileName))) {
            oos.writeObject(map);
        } catch (IOException e) {
            throw new SerializationException("Возникла ошибка при сериализации данных в файл");
        }
    }

    public static HashMap<String, List<String>> deserialize(String serializationFileName) {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(serializationFileName))) {
            return (HashMap<String, List<String>>) oos.readObject();
        } catch (FileNotFoundException e) {
            throw new SerializationException("Не найден файл для десериализации");
        } catch (IOException e) {
            return new HashMap<>();
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Проблемы с кастованием при десериализации");
        }
    }
}
