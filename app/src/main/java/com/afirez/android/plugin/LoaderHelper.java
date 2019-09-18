package com.afirez.android.plugin;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class LoaderHelper {
    public static void combineDexElements(ClassLoader pluginLoader) {
        ClassLoader originPathLoader = PluginApp.getContext().getClassLoader();
        Object originPathList = getPathList(originPathLoader);
        Object pluginPathList = getPathList(pluginLoader);
        Object originElements = getDexElements(originPathList);
        Object pluginElements = getDexElements(pluginPathList);
        Object combineElements = combineDexElements(originElements, pluginElements);
        setDexElements(originPathList, combineElements);
    }

    private static Object getPathList(ClassLoader loader) {
        try {
            Class<?> baseLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathList = baseLoaderClass.getDeclaredField("pathList");
            pathList.setAccessible(true);
            Object o = pathList.get(loader);
            return o;
        } catch (Throwable e) {
            Log.e("LoaderHelper", "getPathList", e);
        }
        return null;
    }

    private static Object getDexElements(Object o) {
        if (o == null) {
            return null;
        }
        Class<?> PathListClass = o.getClass();
        try {
            Field dexElementsField = PathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object elementObj = dexElementsField.get(o);
            return elementObj;
        } catch (Throwable e) {
            Log.e("LoaderHelper", "getDexElements", e);
        }
        return null;
    }

    private static Object combineDexElements(Object originElements, Object pluginElements) {
        Class<?> arrayType = originElements.getClass().getComponentType();
        int originLength = Array.getLength(originElements);
        int pluginLength = Array.getLength(pluginElements);
        int lengths = originLength + pluginLength;
        Object newArray = Array.newInstance(arrayType, lengths);
        for (int i = 0; i < lengths; i++) {
            if (i < originLength) {
                Array.set(newArray, i, Array.get(originElements, i));
            } else {
                Array.set(newArray, i, Array.get(pluginElements, i - originLength));
            }
        }
        return newArray;
    }

    private static void setDexElements(Object originPathList, Object combineElements) {
        try {
            Field dexElementsField = originPathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            dexElementsField.set(originPathList, combineElements);
            Log.w("LoaderHelper", "setDexElements " + combineElements);
        } catch (Throwable e) {
            Log.e("LoaderHelper", "setDexElements", e);
        }
    }
}
