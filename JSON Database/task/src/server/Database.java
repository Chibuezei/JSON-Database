package server;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private final Map<String, String> database = new HashMap<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final String path = System.getProperty("user.dir") + "/src/server/data/db.json";
//    private static final String path = System.getProperty("user.dir") + "/JSON Database/task/src/server/data/db.json";


    public String setValue(String key, String value) {
        database.put(key, value);
        writeToFile(database);
        return toJson(Map.of("response", "OK"));
    }

    public String getValue(String key) {
        readFromFile();
        if (!database.containsKey(key) || database.get(key).isEmpty()) {
            database.put(key, "");
            return toJson(Map.of("response", "ERROR", "reason", "No such key"));
        } else {
            return toJson(Map.of("response", "OK", "value", database.get(key)));
        }
    }

    public String deleteValue(String key) {
        readFromFile();
        if (!database.containsKey(key) || database.get(key).isEmpty()) {
            return toJson(Map.of("response", "ERROR", "reason", "No such key"));
        } else {
            database.put(key, "");
            writeToFile(database);
            return toJson(Map.of("response", "OK"));
        }

    }

    private String toJson(Object object) {
        return new Gson().toJson(object);
    }

    private void writeToFile(Map<String, String> database) {
        writeLock.lock();
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : database.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        try {
            properties.store(new FileOutputStream(path), null);
        } catch (IOException ex) {
            System.out.printf("save exception occurred %s", ex.getMessage());
        }
        writeLock.unlock();
    }

    private void readFromFile() {
        readLock.lock();
        database.clear();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException ex) {
            System.out.printf("save exception occurred %s", ex.getMessage());
        }

        for (String key : properties.stringPropertyNames()) {
            database.put(key, properties.get(key).toString());
        }
        readLock.unlock();
    }
}
