package com.androidx.ulife.utlis;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**

 * Author : weibo.jin
 * Date : 2021/10/19-23:17
 * Description :
 */
public class SystemPropertiesUtil {
    public static final String TAG = "SystemPropertiesProxy";

    public static String getString(Context context, String key) {
        String result = "";
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;
            Method getString = SystemProperties.getMethod("get", paramTypes);

            Object[] params = new Object[1];
            params[0] = new String(key);
            result = (String) getString.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
        return result;
    }

    public static String getString(Context context, String key, String def) {
        String result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;
            Method getString = SystemProperties.getMethod("get", paramTypes);

            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new String(def);
            result = (String) getString.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
        return result;
    }

    public static Integer getInt(Context context, String key, int def) {
        Integer result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);

            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Integer(def);
            result = (Integer) getInt.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
        return result;
    }

    public static Long getLong(Context context, String key, long def) {
        Long result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = long.class;
            Method getLong = SystemProperties.getMethod("getLong", paramTypes);

            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Long(def);
            result = (Long) getLong.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
        return result;
    }

    public static Boolean getBoolean(Context context, String key, boolean def) {
        Boolean result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = boolean.class;
            Method getBoolean = SystemProperties.getMethod("getBoolean", paramTypes);

            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Boolean(def);
            result = (Boolean) getBoolean.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
        return result;
    }

    public static void set(Context context, String key, String val) {
        try {
            @SuppressWarnings("rawtypes")
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;
            Method set = SystemProperties.getMethod("set", paramTypes);

            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new String(val);
            set.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getString IllegalArgumentException: " + e);
        } catch (Exception e) {
            Log.w(TAG, "getString Exception: " + e);
        }
    }
}
