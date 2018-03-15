# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.wuxiaosu.rimethelper.activity.MainActivity {
    private void showModuleActiveInfo(...);
}
-keep class com.wuxiaosu.rimethelper.Main {
    public void handleLoadPackage(...);
}

# 搜索
-keep   class com.amap.api.services.**{*;}

# 2D地图
-keep class com.amap.api.col.**{*;}
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

-dontwarn  com.amap.api.location.**
-dontwarn  com.amap.api.maps2d.**
-dontwarn  com.amap.api.mapcore2d.**
-dontwarn  com.amap.apis.utils.**
-dontwarn  com.amap.api.maps.**