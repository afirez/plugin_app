package com.afirez.android.plugin;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ActivityHelper {

    public static final String THE_INTENT = "THE_INTENT";

    public static void hookAm() {
        try {
            Object singletonObj;

            if (Build.VERSION.SDK_INT < 27) {
                Class<?> forname = Class.forName("android.app.ActivityManagerNative");
                Field defaultField = forname.getDeclaredField("gDefault");
                defaultField.setAccessible(true);
                singletonObj = defaultField.get(null);
            } else {
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                Field singletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
                singletonField.setAccessible(true);
                singletonObj = singletonField.get(null);
            }

            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstance = singletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            Object iActivityManager = mInstance.get(singletonObj);

            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                    , new Class<?>[]{iActivityManagerInterface}, new AmHandler(iActivityManager));
            mInstance.set(singletonObj, proxy);
        } catch (Throwable e) {
            Log.e("LoaderHelper", "hookAm", e);
        }
    }

    public static void hookHandler() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);

            Field mH = activityThreadClass.getDeclaredField("mH");
            mH.setAccessible(true);
            Handler mHObj = (Handler) mH.get(activityThread);

            Field callback = Handler.class.getDeclaredField("mCallback");
            callback.setAccessible(true);
            callback.set(mHObj, new HC(mHObj));
        } catch (Exception e) {
            Log.e("LoaderHelper", "hookHandler", e);
        }
    }

    private static class AmHandler implements InvocationHandler {

        private Object am;

        public AmHandler(Object am) {
            this.am = am;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("startActivity")) {
                Log.d("ActivityHelper", "startActivity wrap ...");

                Intent theIntent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        theIntent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }
                String packageName = PluginApp.getContext().getPackageName();
                Intent holderIntent = new Intent();
                if (theIntent != null) {
                    ComponentName componentName = new ComponentName(packageName, HolderActivity.class.getName());
                    holderIntent.setComponent(componentName);
                    holderIntent.putExtra(ActivityHelper.THE_INTENT, theIntent);
                    args[index] = holderIntent;
                    Log.d("ActivityHelper", "startActivity holderIntent wrapped");
                } else {
                    Log.d("ActivityHelper", "startActivity theIntent == null");
                }
                return method.invoke(am, args);
            }

            Log.d("ActivityHelper", methodName + " no wrap ...");
            return method.invoke(am, args);
        }
    }

    private static class HC implements Handler.Callback {

        private Handler mH;

        private int launchActivity = 100;

        public HC(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == launchActivity) {
                Log.d("ActivityHelper", "mH.handleMessage unwrap...");
                handleLaunchActivity(msg);
            } else {
                Log.d("ActivityHelper", "mH.handleMessage no unwrap...");
            }
            mH.handleMessage(msg);
            return true;
        }

        private void handleLaunchActivity(Message msg) {
            Object activityClientRecord = msg.obj;
            try {
                Field intent = activityClientRecord.getClass().getDeclaredField("intent");
                intent.setAccessible(true);
                Intent holderIntent = (Intent) intent.get(activityClientRecord);
                Intent theIntent = holderIntent.getParcelableExtra(ActivityHelper.THE_INTENT);
                if (theIntent != null) {
                    ComponentName component = theIntent.getComponent();
                    holderIntent.setComponent(component);
                    if (component != null) {
                        holderIntent.putExtra("theActivity", component.getClassName());
                    }
                }
                Log.d("ActivityHelper", "mH.handleMessage unwrapped " + holderIntent.getComponent());
            } catch (Exception e) {
                Log.e("LoaderHelper", "handleLaunchActivity", e);
            }
        }
    }
}
