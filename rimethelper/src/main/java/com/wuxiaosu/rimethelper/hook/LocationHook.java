package com.wuxiaosu.rimethelper.hook;

import com.wuxiaosu.rimethelper.BuildConfig;
import com.wuxiaosu.widget.SettingLabelView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by su on 2018/3/15.
 * 定位 hook
 */

public class LocationHook {
    private XSharedPreferences xsp;

    private boolean fakeLocation;
    private boolean fakeLocationTime;
    private String startTime;
    private String latitude;
    private String longitude;


    public LocationHook(String versionName) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
    }

    public void hook(final ClassLoader classLoader) {
        try {
            final Class aMapLocationClientClazz =
                    XposedHelpers.findClass("com.amap.api.location.AMapLocationClient", classLoader);
            XposedBridge.hookAllConstructors(aMapLocationClientClazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Field[] fields = aMapLocationClientClazz.getDeclaredFields();
                    for (Field field : fields) {
                        Object object = XposedHelpers.getObjectField(param.thisObject, field.getName());
                        if (object != null &&
                                !(object.getClass().getName()).equals("com.alibaba.android.rimet.LauncherApplication")) {
                            Class locationManagerClazz =
                                    XposedHelpers.findClass(object.getClass().getName(), classLoader);
                            hookLocationManager(locationManagerClazz);
                        }
                    }
                    super.afterHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            XposedBridge.log(e);
        }
    }

    private void reload() {
        xsp.reload();
        fakeLocation = xsp.getBoolean("fake_location", false);
        fakeLocationTime = xsp.getBoolean("fake_location_time", false);
        startTime = xsp.getString("location_start_time", "8:40");
        latitude = xsp.getString("latitude", "39.908692");
        longitude = xsp.getString("longitude", "116.397477");
    }


    private void hookLocationManager(Class locationManagerClazz) {
        try {
            //chat location
            XposedBridge.hookAllMethods(locationManagerClazz, "setLocationListener", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Class listenerClazz = param.args[0].getClass();
                    XposedBridge.hookAllMethods(listenerClazz, "onLocationChanged", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = fakeAMapLocationObject(param.args[0]);
                            super.beforeHookedMethod(param);
                        }
                    });
                    super.beforeHookedMethod(param);
                }
            });
            //web location
            Method[] methods = locationManagerClazz.getMethods();
            for (Method method : methods) {
                Class[] classes = method.getParameterTypes();
                if (classes.length == 1 && classes[0].getName().equals("com.amap.api.location.AMapLocation")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = fakeAMapLocationObject(param.args[0]);
                            super.beforeHookedMethod(param);
                        }
                    });
                    break;
                }
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private Object fakeAMapLocationObject(Object object) {
        reload();
        if (fakeLocation) {
            if (!fakeLocationTime || isAfterSetTime(startTime)) {
                XposedHelpers.callMethod(object, "setLatitude", Double.valueOf(latitude));
                XposedHelpers.callMethod(object, "setLongitude", Double.valueOf(longitude));
            }
        }
        return object;
    }

    /**
     * 当前时间在设置时间之后
     *
     * @param setTime 01:12
     * @return
     */
    private boolean isAfterSetTime(String setTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = null;
        Date now = null;
        try {
            date = sdf.parse(setTime);
        } catch (ParseException e) {
            try {
                date = sdf.parse("8:40");
            } catch (ParseException ignored) {
            }
        }
        Calendar calendar = Calendar.getInstance();
        try {
            now = sdf.parse(calendar.get(Calendar.HOUR_OF_DAY)
                    + ":" + calendar.get(Calendar.MINUTE));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() < now.getTime();
    }
}
