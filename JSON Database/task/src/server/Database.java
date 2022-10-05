package server;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, String> database = new HashMap<>();

    public String setValue(String key, String value) {
        database.put(key, value);
        return toJson(Map.of("response", "OK"));
    }

    public String getValue(String key) {
        if (!database.containsKey(key) || database.get(key).isEmpty()) {
            database.put(key, "");
            return toJson(Map.of("response", "ERROR", "reason", "No such key"));
        } else {
            return toJson(Map.of("response", "OK", "value", database.get(key)));
        }
//        System.out.println(database.getOrDefault(key, "ERROR"));
    }

    public String deleteValue(String key) {
        if (!database.containsKey(key) || database.get(key).isEmpty()) {
            return toJson(Map.of("response", "ERROR", "reason", "No such key"));
        } else {
            database.put(key, "");
            return toJson(Map.of("response", "OK"));
        }

    }

    private String toJson(Object object) {
        return new Gson().toJson(object);
    }

}
