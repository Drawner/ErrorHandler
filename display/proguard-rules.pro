# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Programs\Tools\AndroidSDK/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Add any project specific keep options here:
-keep class com.andrious.errorhandler.display.ErrorHandler { *; }


# use this option to remove logging code.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# It's being sought but doesn't need to be.
-dontwarn org.joda.convert.**

# Necessary since adding compile files('../../../libs/opencsv/opencsv-3.3.jar')
-dontwarn org.apache.commons.collections.BeanMap
-dontwarn java.beans.**

##---------------Begin: proguard configuration common for all Android apps ----------

# Try a little more passes for kicks.
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

#To safely unpack jars on Windows. Obfuscated jars will become slightly larger.
#-dontusemixedcaseclassnames
#Specifies not to ignore non-public library classes. As of version 4.5, this is the default setting.
-dontskipnonpubliclibraryclasses
#Don't ignore package visible library class members (fields and methods).
-dontskipnonpubliclibraryclassmembers
# Only when eventually targeting Android, it is not necessary, so you can then switch it off to reduce the processing time a bit.
-dontpreverify
# If there's an exception, this will print out a stack trace, instead of just the exception message.
 -verbose
 # This can improve the results of the optimization step.
 # Only applicable when obfuscating with the -repackageclasses option.
 -allowaccessmodification
 ## Removes package names making the code even smaller and less comprehensible.
 -repackageclasses ''


#Describes the internal structure of all the class files in the APK.
# dump.txt
-dump class_files.txt
#Lists the classes and members that were not obfuscated.
-printseeds seeds.txt
-printusage unused.txt
# Provides a translation between the original and obfuscated class, method, and field names.
-printmapping mapping.txt


##------------- Lets obfuscated code produce stack traces that can still be deciphered later on
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
# Preserve all native method names and the names of their classes.
-keepclasseswithmembers class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

##---------------End: proguard configuration common for all Android apps

##--------------- Understand the @Keep support annotation.
-keepattributes *Annotation*

-keep class android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
