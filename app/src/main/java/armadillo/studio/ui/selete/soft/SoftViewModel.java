package armadillo.studio.ui.selete.soft;


import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import armadillo.studio.CloudApp;
import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.base.callback.GetAppCallBack;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.FileSize;
import armadillo.studio.model.apk.PackageInfos;

public class SoftViewModel extends BaseViewModel<List<PackageInfos>> {
    @Override
    public List<PackageInfos> getValue() {
        return null;
    }

    public void getAll(GetAppCallBack callBack, Activity activity) {
        CloudApp.getCachedThreadPool().execute(()->{
            List<PackageInfos> newpackages = new ArrayList<>();
            try {
                PackageManager pm = CloudApp.getContext().getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? PackageManager.MATCH_UNINSTALLED_PACKAGES : PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo packageInfo : packages) {
                    if (activity == null)
                        return;
                    if (activity.isDestroyed())
                        return;
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                            && packageInfo.applicationInfo.sourceDir != null
                            && new File(packageInfo.applicationInfo.sourceDir).canRead()) {
                        PackageInfos infos = new PackageInfos();
                        infos.setPackageInfo(packageInfo);
                        infos.setName(packageInfo.applicationInfo.loadLabel(pm).toString());
                        infos.setSize(FileSize.getAutoFileOrFileSize(packageInfo.applicationInfo.sourceDir));
                        infos.setIco(AppUtils.getApkDrawable(packageInfo.applicationInfo.sourceDir));
                        AnalysisJiaGu(packageInfo.applicationInfo.sourceDir, infos);
                        newpackages.add(infos);
                    }
                }
                Collections.sort(newpackages, (packageInfo, t1) -> {
                    if (packageInfo.getPackageInfo().applicationInfo.sourceDir == null)
                        return -1;
                    if (t1.getPackageInfo().applicationInfo.sourceDir == null)
                        return -1;
                    File src = new File(packageInfo.getPackageInfo().applicationInfo.sourceDir);
                    File old = new File(t1.getPackageInfo().applicationInfo.sourceDir);
                    if (src.lastModified() < old.lastModified())
                        return 1;
                    else
                        return -1;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callBack.Next(newpackages);
            });
        });
    }

    private void AnalysisJiaGu(String path, PackageInfos info) {
        if (path == null) {
            info.setJiagu("未检测到加固");
            info.setJiagu_flag(false);
            return;
        }
        try (ZipFile zipFile = new ZipFile(path)) {
            ZipEntry zipEntry = zipFile.getEntry("assets/libjiagu.so");
            if (zipEntry != null) {
                info.setJiagu("360加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("assets/ijm_lib/armeabi/libexec.so");
            if (zipEntry != null) {
                info.setJiagu("爱加密");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libkdp.so");
            if (zipEntry != null) {
                info.setJiagu("几维加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libSecShell.so");
            if (zipEntry != null) {
                info.setJiagu("梆梆加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/DexHelper.so");
            if (zipEntry != null) {
                info.setJiagu("梆梆定制版加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/mix.dex");
            if (zipEntry != null) {
                info.setJiagu("腾讯加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("assets/libtosprotection.armeabi-v7a.so");
            if (zipEntry != null) {
                info.setJiagu("腾讯御安全");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libx3g.so");
            if (zipEntry != null) {
                info.setJiagu("顶象加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("assets/libzuma.so");
            if (zipEntry != null) {
                info.setJiagu("阿里加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("assets/dp.arm.so.dat");
            if (zipEntry != null) {
                info.setJiagu("dexprotect加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libbaiduprotect.so");
            if (zipEntry != null) {
                info.setJiagu("百度加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libapktoolplus_jiagu.so");
            if (zipEntry != null) {
                info.setJiagu("apktoolplus加固");
                info.setJiagu_flag(true);
                return;
            }
            zipEntry = zipFile.getEntry("lib/armeabi/libitsec.so");
            if (zipEntry != null) {
                info.setJiagu("海云安加固");
                info.setJiagu_flag(true);
                return;
            }
            info.setJiagu("未检测到加固");
            info.setJiagu_flag(false);
        } catch (Exception e) {
            info.setJiagu("未检测到加固");
            info.setJiagu_flag(false);
        }
    }
}
