package com.android.performanceanalysis.hook;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;

/**
 * 监控Java线程的创建和销毁
 */
public class ThreadMethodHook extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        if (param != null && param.thisObject != null) {
            Thread t = (Thread) param.thisObject;
            Log.i("ThreadMethodHook", "thread:" + t + ", started..");
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        if (param != null && param.thisObject != null) {
            Thread t = (Thread) param.thisObject;
            Log.i("ThreadMethodHook", "thread:" + t + ", exit..");
        }
    }
}