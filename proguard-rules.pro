# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Volumes/SATA/Android/SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#打印混淆信息
-verbose
#代码优化选项，不加该行会将没有用到的类删除，这里为了验证时间结果而使用，在实际生产环境中可根据实际需要选择是否使用
-dontshrink
-dontwarn androidx.annotation.Keep
#保留注解，如果不添加改行会导致我们的@Keep注解失效
-keepattributes *Annotation*
-keep @androidx.annotation.Keep class **{
@androidx.annotation.Keep <fields>;
@androidx.annotation.Keep <methods>;
}
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

## ----- Begin: Retrofit -----
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
## ----- End: Retrofit -----

## ----- Begin: Okio -----
-dontwarn okio.**
## ----- End: Okio -----

## ----- Begin: Gson -----
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
## ----- End: Gson -----
-keepparameternames

 # 对于R（资源）类中的静态属性不能被混淆
-keepclassmembers class **.R$* {
 public static <fields>;
}
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep public class **.*samsung*.** {*;}
-keep public class **.*facebook*.** {*;}
-keep public class **.*Base*.** {*;}
-keep public class **.*base*.** {*;}
-keep public class **.*util*.** {*;}
-keep public class **.*Holder** {*;}
-keep public class **.unity.* {*;}

