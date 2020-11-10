package com.wuxiaosu.rimethelper.hook;

import android.annotation.SuppressLint;

import com.wuxiaosu.rimethelper.BuildConfig;
import com.wuxiaosu.widget.SettingLabelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by su on 2018/3/15.
 * 定位 hook
 */

public class LocationHook {

    private static XSharedPreferences sXsp;

    private static boolean sFakeLocation;
    private static boolean sFakeLocationTime;
    private static String sStartTime;
    private static String sLatitude;
    private static String sLongitude;

    private static final List<String> LISTENER_CLASS = new ArrayList<>();

    public static void hook(final ClassLoader classLoader) {
        try {
            final Class<?> aMapLocationClientClazz =
                    XposedHelpers.findClass("com.amap.api.location.AMapLocationClient", classLoader);
            XposedBridge.hookAllMethods(aMapLocationClientClazz, "setLocationListener", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param.args.length == 1) {
                        Class<?> listenerClazz = param.args[0].getClass();
                        if (!LISTENER_CLASS.contains(listenerClazz.getName())) {
                            LISTENER_CLASS.add(listenerClazz.getName());
                            XposedBridge.hookAllMethods(listenerClazz, "onLocationChanged", new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    param.args[0] = fakeAMapLocationObject(param.args[0]);
                                    super.beforeHookedMethod(param);
                                }
                            });
                        }
                    }
                }
            });
        } catch (Error | Exception e) {
            XposedBridge.log(e);
        }
    }

    private static void reload() {
        if (sXsp == null) {
            sXsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
            sXsp.makeWorldReadable();
        }
        sXsp.reload();
        sFakeLocation = sXsp.getBoolean("fake_location", false);
        sFakeLocationTime = sXsp.getBoolean("fake_location_time", false);
        sStartTime = sXsp.getString("location_start_time", "8:40");
        sLatitude = sXsp.getString("latitude", "39.908692");
        sLongitude = sXsp.getString("longitude", "116.397477");
    }

    private static Object fakeAMapLocationObject(Object object) {
        reload();
        if (sFakeLocation) {
            if (!sFakeLocationTime || isAfterSetTime(sStartTime)) {
                XposedHelpers.callMethod(object, "setLatitude", Double.valueOf(sLatitude));
                XposedHelpers.callMethod(object, "setLongitude", Double.valueOf(sLongitude));
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
    @SuppressLint("SimpleDateFormat")
    private static boolean isAfterSetTime(String setTime) {
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
