package com.ueueo.cache;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Lee on 16/7/11.
 */
public class ContentProviderCache implements Cache {

    private static final String DEFAULT_NAME = "default";

    private String mSchemaName = null;

    private Application mApplication = null;

    private ContentProviderCache() {
    }

    public ContentProviderCache(@Nullable Application application) {
        this(application, DEFAULT_NAME);
    }

    public ContentProviderCache(@Nullable Application application, @Nullable String name) {
        this.mApplication = application;
        this.mSchemaName = name;
    }

    @Override
    public void putString(@Nullable String key, @Nullable String value) {
        putString(key, value, 0);
    }

    @Override
    public void putString(@Nullable String key, @Nullable String value, long expiresIn) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (value == null) {
            remove(key);
            return;
        }
        ContentResolver contentResolver = mApplication.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("schema", mSchemaName);
        values.put("key", key);
        values.put("value", value);
        values.put("expiresIn", expiresIn > 0 ? (expiresIn + System.currentTimeMillis()) : expiresIn);
        if (contentResolver.update(CacheProvider.URI, values, "schema = ? and key = ?", new String[]{mSchemaName, key}) <= 0) {
            contentResolver.insert(CacheProvider.URI, values);
        }
    }

    @Override
    public String getString(@Nullable String key, String defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        ContentResolver contentResolver = mApplication.getContentResolver();
        Cursor cursor = contentResolver.query(CacheProvider.URI, new String[]{"value", "expiresIn"}, "schema = ? and key = ?", new String[]{mSchemaName, key}, null);
        if (cursor != null && cursor.moveToFirst()) {
            long expiresIn = cursor.getLong(1);
            if (expiresIn > 0 && System.currentTimeMillis() >= expiresIn) {
                remove(key);
            } else {
                String value = cursor.getString(0);
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public void putInt(@Nullable String key, int value) {
        putInt(key, value, 0);
    }

    @Override
    public void putInt(@Nullable String key, int value, long expiresIn) {
        putString(key, String.valueOf(value), expiresIn);
    }

    @Override
    public int getInt(@Nullable String key, int defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putFloat(@Nullable String key, float value) {
        putFloat(key, value, 0);
    }

    @Override
    public void putFloat(@Nullable String key, float value, long expiresIn) {
        putString(key, String.valueOf(value), expiresIn);
    }

    @Override
    public float getFloat(@Nullable String key, float defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putLong(@Nullable String key, long value) {
        putLong(key, value, 0);
    }

    @Override
    public void putLong(@Nullable String key, long value, long expiresIn) {
        putString(key, String.valueOf(value), expiresIn);
    }

    @Override
    public long getLong(@Nullable String key, long defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putBoolean(@Nullable String key, boolean value) {
        putBoolean(key, value, 0);
    }

    @Override
    public void putBoolean(@Nullable String key, boolean value, long expiresIn) {
        putString(key, String.valueOf(value), expiresIn);
    }

    @Override
    public boolean getBoolean(@Nullable String key, boolean defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    @Override
    public void putBytes(@Nullable String key, @Nullable byte[] value) {
        putBytes(key, value, 0);
    }

    @Override
    public void putBytes(@Nullable String key, @Nullable byte[] value, long expiresIn) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (value == null) {
            remove(key);
            return;
        }
        try {
            putString(key, new String(value, "UTF-8"), expiresIn);
        } catch (UnsupportedEncodingException e) {
        }
    }

    @Override
    public byte[] getBytes(@Nullable String key, byte[] defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putJSONObject(@Nullable String key, @Nullable JSONObject value) {
        putJSONObject(key, value, 0);
    }

    @Override
    public void putJSONObject(@Nullable String key, @Nullable JSONObject value, long expiresIn) {
        if (value != null) {
            putString(key, value.toString(), expiresIn);
        } else {
            remove(key);
        }
    }

    @Override
    public JSONObject getJSONObject(@Nullable String key, JSONObject defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return new JSONObject(value);
            } catch (JSONException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putJSONArray(@Nullable String key, @Nullable JSONArray value) {
        putJSONArray(key, value, 0);
    }

    @Override
    public void putJSONArray(@Nullable String key, @Nullable JSONArray value, long expiresIn) {
        if (value != null) {
            putString(key, value.toString(), expiresIn);
        } else {
            remove(key);
        }
    }

    @Override
    public JSONArray getJSONArray(@Nullable String key, JSONArray defaultValue) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            try {
                return new JSONArray(value);
            } catch (JSONException e) {
            }
        }
        return defaultValue;
    }

    @Override
    public void putObject(@Nullable String key, @Nullable Object value) {
        putObject(key, value, 0);
    }

    @Override
    public void putObject(@Nullable String key, @Nullable Object value, long expiresIn) {
        if (value != null) {
            putString(key, new Gson().toJson(value), expiresIn);
        } else {
            remove(key);
        }
    }

    @Override
    public <T> T getObject(@Nullable String key, T defaultValue, Class<T> classOfT) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            return new Gson().fromJson(value, classOfT);
        }
        return defaultValue;
    }

    @Override
    public void putObjectArray(@Nullable String key, @Nullable List value) {
        putObjectArray(key, value, 0);
    }

    @Override
    public void putObjectArray(@Nullable String key, @Nullable List value, long expiresIn) {
        if (value != null) {
            putString(key, new Gson().toJson(value), expiresIn);
        } else {
            remove(key);
        }
    }

    @Override
    public <T> List<T> getObjectArray(@Nullable String key, @Nullable List<T> defaultValue,Type type) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            return new Gson().fromJson(value, type);
        }
        return defaultValue;
    }

    @Override
    public void putObjectMap(@Nullable String key, @Nullable Map value) {
        putObjectMap(key, value, 0);
    }

    @Override
    public void putObjectMap(@Nullable String key, @Nullable Map value, long expiresIn) {
        if (value != null) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            putString(key, gson.toJson(value), expiresIn);
        } else {
            remove(key);
        }
    }

    @Override
    public <K, V> Map<K, V> getObjectMap(@Nullable String key, @Nullable Map<K, V> defaultValue,Type type) {
        String value = getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            return gson.fromJson(value, type);
        }
        return defaultValue;
    }

    @Override
    public void remove(@Nullable String key) {
        mApplication.getContentResolver().delete(CacheProvider.URI, "schema = ? and key = ?", new String[]{mSchemaName, key});
    }

    @Override
    public void clear() {
        mApplication.getContentResolver().delete(CacheProvider.URI, "schema = ?", new String[]{mSchemaName});
    }

    @Override
    public boolean contains(@Nullable String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        ContentResolver contentResolver = mApplication.getContentResolver();
        Cursor cursor = contentResolver.query(CacheProvider.URI, new String[]{"expiresIn"}, "schema = ? and key = ?", new String[]{mSchemaName, key}, null);
        if (cursor.moveToFirst()) {
            long expiresIn = cursor.getLong(0);
            if (expiresIn > 0 && System.currentTimeMillis() >= expiresIn) {
                remove(key);
            } else {
                return true;
            }
        }
        return false;
    }
}
