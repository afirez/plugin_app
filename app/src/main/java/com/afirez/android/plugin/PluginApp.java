package com.afirez.android.plugin;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Method;

public class PluginApp extends Application {

    private static Context mContext;
    private AssetManager assetManager;
    private Resources.Theme mTheme;
    private Resources newResource;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mContext = base;

        try {
            String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "plugin.apk";

            assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, apkPath);

            Method ensureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocks.setAccessible(true);
            ensureStringBlocks.invoke(assetManager);

            Resources superRes = getResources();
            newResource = new Resources(assetManager, superRes.getDisplayMetrics()
                    , superRes.getConfiguration());
            mTheme = newResource.newTheme();
            mTheme.setTo(PluginApp.getContext().getTheme());

            String cachePath = getCacheDir().getAbsolutePath();
            DexClassLoader pluginLoader = new DexClassLoader(apkPath, cachePath, cachePath, getClassLoader());
            LoaderHelper.combineDexElements(pluginLoader);

            ActivityHelper.hookAm();
            ActivityHelper.hookHandler();
        } catch (Throwable e) {
            Log.e("LoaderHelper", "load ", e);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }

    @Override
    public Resources getResources() {
        return newResource == null ? super.getResources() : newResource;
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme() : mTheme;
    }
}