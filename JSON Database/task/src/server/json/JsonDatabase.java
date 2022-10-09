package server.json;

import com.google.gson.*;
import server.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static server.json.JsonDbUtils.*;

public class JsonDatabase {
    private static final String path = System.getProperty("user.dir") + "/src/server/data/db.json";
    public static final String ERROR_NO_SUCH_KEY = "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
    public static final String ERROR_INCORRECT_JSON = "{\"response\":\"ERROR\",\"reason\":\"Incorrect JSON\"}";
    public static final String OK = "{\"response\":\"OK\"}";
    private final Path DB_PATH;
    private final JsonObject db;
    private final ReentrantReadWriteLock RReadWriteLock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock r = RReadWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock w = RReadWriteLock.writeLock();

    public JsonDatabase() throws IOException {
        this.DB_PATH = Path.of(path);
        createDbIfNotExists(DB_PATH);
        this.db = readDbFromFile(DB_PATH).getAsJsonObject();

    }

    /**
     * input "{"type":"set","key":"10","value":"some data"}"
     *
     * @return String
     */
    public String executeJson(String json) throws IOException {
        JsonObject jo;
        String type;
        JsonElement key;
        JsonElement value = null;
        String result;
        try {
            Request request = new Gson().fromJson(json, Request.class);
            jo = JsonParser.parseString(json).getAsJsonObject();
            type = request.getType();
            key = request.getKey();
            if (type.equals("set")) {
                value = request.getValue();
            }
        } catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
            return ERROR_INCORRECT_JSON;
        }
        if ((value == null && jo.size() != 2) || (value != null && jo.size() != 3)) {
            return ERROR_INCORRECT_JSON;
        }
        try {
            switch (type) {
                case "get" -> result = new Gson().toJson(Map.of("response", "OK", "value", get(key)));
                case "set" -> {
                    set(key, value);
                    result = OK;
                }
                case "delete" -> {
                    delete(key);
                    result = OK;
                }
                default -> result = ERROR_INCORRECT_JSON;
            }
        } catch (NoSuchKeyException exp) {
            return ERROR_NO_SUCH_KEY;
        }
        return result;
    }

    public JsonElement get(JsonElement key) {
        try {
            r.lock();
            if (key.isJsonPrimitive() && db.has(key.getAsString())) {
                return db.get(key.getAsString());
            } else if (key.isJsonArray()) {
                return findElement(key.getAsJsonArray(), false);
            }
            throw new NoSuchKeyException();
        } finally {
            r.unlock();
        }
    }

    public void delete(JsonElement key) throws IOException {
        try {
            w.lock();
            if (key.isJsonPrimitive() && db.has(key.getAsString())) {
                db.remove(key.getAsString());
            } else if (key.isJsonArray()) {
                JsonArray keys = key.getAsJsonArray();
                String toRemove = keys.remove(keys.size() - 1).getAsString();
                findElement(keys, false).getAsJsonObject().remove(toRemove);
                writeDbToFile(db, DB_PATH);
            } else {
                throw new NoSuchKeyException();
            }
        } finally {
            w.unlock();
        }
    }

    public void set(JsonElement key, JsonElement value) throws IOException {
        try {
            w.lock();
            if (key.isJsonPrimitive()) {
                db.add(key.getAsString(), value);
            } else if (key.isJsonArray()) {
                JsonArray keys = key.getAsJsonArray();
                String toAdd = keys.remove(keys.size() - 1).getAsString();
                findElement(keys, true).getAsJsonObject().add(toAdd, value);
            } else {
                throw new NoSuchKeyException();
            }
            writeDbToFile(db, DB_PATH);
        } finally {
            w.unlock();
        }
    }

    private JsonElement findElement(JsonArray keys, boolean createIfAbsent) {
        JsonElement tmp = db;
        if (createIfAbsent) {
            for (JsonElement key : keys) {
                if (!tmp.getAsJsonObject().has(key.getAsString())) {
                    tmp.getAsJsonObject().add(key.getAsString(), new JsonObject());
                }
                tmp = tmp.getAsJsonObject().get(key.getAsString());
            }
        } else {
            for (JsonElement key : keys) {
                if (!key.isJsonPrimitive() || !tmp.getAsJsonObject().has(key.getAsString())) {
                    throw new NoSuchKeyException();
                }
                tmp = tmp.getAsJsonObject().get(key.getAsString());
            }
        }
        return tmp;
    }

}