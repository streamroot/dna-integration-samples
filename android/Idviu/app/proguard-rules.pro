# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/benoit/Dev/SDK/adt-bundle-mac-x86_64/sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable


# ACRA needs "annotations" so add this...
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.view.View
-keep public class android.support.**
-keep public class com.google.**
-keep public class ch.boye.**
-keep public class com.googlecode.**
-keep public class com.labgency.hss.views.**
-dontwarn android.support.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn com.google.**

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * {
    native <methods>;
}


-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepnames enum * implements java.io.Serializable

-keepclassmembers enum * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class com.labgency.tools.security.CryptoManager{
    java.lang.String KEY_KEYPART;
}


-keepclassmembers class com.labgency.splayer.DRMCredentialsHandler{
	*;
}

-keep public class com.labgency.drm.Constraints

-keepclassmembers class com.labgency.drm.Constraints{
	*;
}

-keep class com.labgency.tools.data.utils.PrefFile{
    public java.lang.String getValueString(java.lang.String);
}

-keep class com.labgency.hss.utils.PrefFile{
    public java.lang.String getValueString(java.lang.String);
}


-keepclasseswithmembers class com.labgency.tools.player.drm.*{
	*;
}



-keepclasseswithmembers class com.labgency.splayer.*{
	*;
}

-keepclasseswithmembers interface com.labgency.splayer.*{
	*;
}


-keepclasseswithmembers class com.labgency.tools.player.labgency.*{
	*;
}

-keep class com.labgency.player.*

-keepclasseswithmembers class com.labgency.player.*{
	*;
}

-keep interface com.labgency.player.**{
	*;
}

-keepclasseswithmembers interface com.labgency.player.**{
	*;
}

-keep class com.labgency.splayer.*

-keep class com.labgency.tools.data.utils.PrefFile{
    public java.lang.String getValueString(java.lang.String);
}



-keepclassmembers class com.labgency.hss.HSSLibraryManager{
	com.labgency.hss.HSSLibraryManager getInstance();
	java.lang.String getLibPath(java.lang.String);
	boolean isLibInSob(java.lang.String);
	int loadLibrary(java.lang.String);
}

-keep class com.labgency.hss.HSSDownloadManager{
	com.labgency.hss.HSSDownloadManager getInstance();
	*** getPieceDataForDownloadAtPath(java.lang.String);
	long getFileSizeForDownloadAtPath(java.lang.String);
}



-keep public class com.labgency.hss.HSSClockManager

-keepclassmembers class com.labgency.hss.HSSClockManager{
	public com.labgency.hss.HSSClockManager getInstance();

}

-keep public class com.labgency.tools.security.utils.CUtils

-keepclassmembers class com.labgency.tools.security.utils.CUtils{
	public long getAvailable(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class com.labgency.hss.PK12Wrapper

-keepclassmembers public class com.labgency.hss.PK12Wrapper {
  public long getTime();
  private void saveData(java.lang.String, byte[]);
  private byte[] getData(java.lang.String);
  private byte[] getDeviceID();
}
