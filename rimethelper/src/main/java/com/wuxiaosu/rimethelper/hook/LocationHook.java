package com.wuxiaosu.rimethelper.hook;

import com.wuxiaosu.rimethelper.BuildConfig;
import com.wuxiaosu.widget.SettingLabelView;

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

    private String webClazzName;
    private String otherClazzName;

    public LocationHook(String versionName) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        switch (versionName) {
            case "4.2.0":
                webClazzName = "fym";
                otherClazzName = "bhl";
                break;
            case "4.2.1":
                webClazzName = "fyn";
                otherClazzName = "bhl";
                break;
            case "4.2.6":
                webClazzName = "gif";
                otherClazzName = "bjp";
                break;
            case "4.2.8":
                webClazzName = "gnv";
                otherClazzName = "blt";
                break;
            case "4.3.0":
                webClazzName = "gxq";
                otherClazzName = "brm";
                break;
            default:
            case "4.3.1":
                webClazzName = "gxt";
                otherClazzName = "brm";
                break;
        }
    }

    public void hook(ClassLoader classLoader) {
        try {
            Class webClazz = XposedHelpers.findClass(webClazzName, classLoader);
            XposedBridge.hookAllMethods(webClazz, "setLocationListener", new XC_MethodHook() {
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

            Class otherClazz = XposedHelpers.findClass(otherClazzName, classLoader);
            XposedBridge.hookAllMethods(otherClazz, "onLocationChanged", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = fakeAMapLocationObject(param.args[0]);
                    super.beforeHookedMethod(param);
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
