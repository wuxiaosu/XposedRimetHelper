package com.wuxiaosu.rimethelper;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.wuxiaosu.rimethelper.hook.LocationHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by su on 2018/3/15.
 */

public class Main implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }

        final String RIMET_PACKAGENAME = "com.alibaba.android.rimet";
        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            XposedHelpers.findAndHookMethod("com.wuxiaosu.rimethelper.activity.MainActivity", lpparam.classLoader,
                    "showModuleActiveInfo", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = true;
                            super.beforeHookedMethod(param);
                        }
                    });
        }

        if (lpparam.packageName.equals(RIMET_PACKAGENAME)) {
            try {
                XposedHelpers.findAndHookMethod(Application.class,
                        "attach",
                        Context.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                Context context = (Context) param.args[0];
                                ClassLoader appClassLoader = context.getClassLoader();
                                handleHook(appClassLoader,
                                        getVersionName(context, RIMET_PACKAGENAME));
                            }
                        });
            } catch (Error | Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleHook(ClassLoader classLoader, String versionName) {
        new LocationHook(versionName).hook(classLoader);
    }

    private String getVersionName(Context context, String pkgName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(pkgName, 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
