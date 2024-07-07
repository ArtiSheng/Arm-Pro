/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Objects;

import armadillo.studio.CloudApp;
import armadillo.studio.R;

public class AppUtils {
    @NotNull
    @SuppressLint("HardwareIds")
    public static String GetAndroidId() {
        String id = Settings.Secure.getString(CloudApp.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.isEmpty())
            id = CloudApp.getContext().getString(R.string.unknown);
        return id;
    }

    public static int GetVer(String apkPath) {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info == null)
            return -1;
        info.applicationInfo.sourceDir = apkPath;
        info.applicationInfo.publicSourceDir = apkPath;
        return info.versionCode;
    }

    public static int GetVer() throws PackageManager.NameNotFoundException {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        if (pm != null) {
            PackageInfo info = pm.getPackageInfo(CloudApp.getContext().getPackageName(), 0);
            if (info != null) {
                return info.versionCode;
            }
        }
        return -1;
    }

    public static String GetVerSion() throws PackageManager.NameNotFoundException {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        PackageInfo info = pm.getPackageInfo(CloudApp.getContext().getPackageName(), 0);
        if (info != null) {
            return info.versionName;
        }
        return "Unknown";
    }

    @Nullable
    public static PackageInfo GetPackageInfo(String apkPath) {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info == null)
            return null;
        Objects.requireNonNull(info).applicationInfo.sourceDir = apkPath;
        Objects.requireNonNull(info).applicationInfo.publicSourceDir = apkPath;
        return info;
    }

    @Nullable
    public static PackageInfo GetPackageInfoSigner(String apkPath) {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
        if (info == null)
            return null;
        Objects.requireNonNull(info).applicationInfo.sourceDir = apkPath;
        Objects.requireNonNull(info).applicationInfo.publicSourceDir = apkPath;
        return info;
    }

    public static String getApplicationName(String name) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = CloudApp.getContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(name, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo == null)
            return CloudApp.getContext().getString(R.string.unknown);
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public static void installApk(Context context, String downloadApk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(downloadApk);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getApkDrawable(String apkPath) {
        if (apkPath == null)
            return CloudApp.getContext().getResources().getDrawable(R.mipmap.ic_launcher);
        try {
            PackageManager pm = CloudApp.getContext().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = Objects.requireNonNull(info).applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            return appInfo.loadIcon(pm);
        } catch (Exception e) {
            Log.e("Exception", "Path -> " + apkPath);
            e.printStackTrace();
            return CloudApp.getContext().getResources().getDrawable(R.mipmap.ic_launcher);
        }
    }

    public static byte[] drawableToByte(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }
        return null;
    }

    public static boolean isApkFile(String apkPath) {
        try {
            PackageManager pm = CloudApp.getContext().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info == null)
                return false;
            info.applicationInfo.sourceDir = apkPath;
            info.applicationInfo.publicSourceDir = apkPath;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isApkFile(File apkPath) {
        try {
            if (!apkPath.exists())
                return false;
            PackageManager pm = CloudApp.getContext().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if (info == null)
                return false;
            info.applicationInfo.sourceDir = apkPath.getAbsolutePath();
            info.applicationInfo.publicSourceDir = apkPath.getAbsolutePath();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isInstalled(@NotNull Context context, String packageName) {
        boolean hasInstalled = false;
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm
                .getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo p : list) {
            if (packageName != null && packageName.equals(p.packageName)) {
                hasInstalled = true;
                break;
            }
        }
        return hasInstalled;
    }
}
