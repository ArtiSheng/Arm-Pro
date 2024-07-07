/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.topjohnwu.superuser.Shell;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.jks.SignerApk;
import armadillo.studio.data.InjectData;
import armadillo.studio.model.apk.Ignore;
import armadillo.studio.model.apk.SignerInfo;
import armadillo.studio.model.handle.ResourceRule;
import armadillo.studio.model.signer.KeyInfo;
import armadillo.studio.model.task.TaskInfo;
import armadillo.studio.widget.BaleDialog;
import armadillo.studio.widget.LoadingDialog;

public class ArchiveZip {
    private static final String TAG = ArchiveZip.class.getSimpleName();

    /**
     * 打包任务上传资源
     *
     * @param resourceRules
     * @param Path
     * @return
     */
    @NotNull
    public static InjectData AutoArchive(HashSet<ResourceRule> resourceRules, String Path) {
        try {
            String packName = Objects.requireNonNull(AppUtils.GetPackageInfo(Path)).packageName;
            HashMap<String, JsonElement> rule = new HashMap<>();
            File cacheFile = null;
            if (resourceRules != null && resourceRules.size() > 0) {
                cacheFile = new File(CloudApp.getContext().getCacheDir(), UUID.randomUUID().toString());
                try (FileOutputStream cacheOut = new FileOutputStream(cacheFile);
                     ZipOutputStream zipOutputStream = new ZipOutputStream(cacheOut);
                     ZipFile zipFile = new ZipFile(Path)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    HashSet<String> adds = new HashSet<>();
                    HashSet<String> SoApis = new HashSet<>();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("lib/") && zipEntry.getName().endsWith(".so")) {
                            File temp = new File(zipEntry.getName());
                            if (temp.getParentFile() != null) {
                                File dir = temp.getParentFile();
                                SoApis.add(dir.getName());
                            }
                        }
                        for (ResourceRule resourceRule : resourceRules) {
                            if (adds.contains(zipEntry.getName()))
                                continue;
                            /**
                             * 判断name
                             */
                            if (resourceRule.getName() != null) {
                                if ((resourceRule.getName().startsWith("!") && !zipEntry.getName().equals(resourceRule.getName().substring(1)))
                                        ||
                                        (!resourceRule.getName().startsWith("!") && zipEntry.getName().equals(resourceRule.getName()))) {
                                    adds.add(zipEntry.getName());
                                    zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                                    IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                                    zipOutputStream.closeEntry();
                                }
                            }
                            /**
                             * 判断StartWith
                             */
                            else if (resourceRule.getStartWith() != null
                                    && resourceRule.getEndWith() == null) {
                                if ((resourceRule.getStartWith().startsWith("!") && !zipEntry.getName().startsWith(resourceRule.getStartWith().substring(1)))
                                        ||
                                        (!resourceRule.getStartWith().startsWith("!") && zipEntry.getName().startsWith(resourceRule.getStartWith()))) {
                                    adds.add(zipEntry.getName());
                                    zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                                    IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                                    zipOutputStream.closeEntry();
                                }
                            }
                            /**
                             * 判断EndWith
                             */
                            else if (resourceRule.getEndWith() != null
                                    && resourceRule.getStartWith() == null) {
                                if ((resourceRule.getEndWith().startsWith("!") && !zipEntry.getName().endsWith(resourceRule.getEndWith().substring(1)))
                                        ||
                                        (!resourceRule.getEndWith().startsWith("!") && zipEntry.getName().endsWith(resourceRule.getEndWith()))) {
                                    adds.add(zipEntry.getName());
                                    zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                                    IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                                    zipOutputStream.closeEntry();
                                }
                            }
                            /**
                             * 判断StartWith EndWith
                             */
                            else if (resourceRule.getEndWith() != null
                                    && resourceRule.getStartWith() != null
                                    && zipEntry.getName().startsWith(resourceRule.getStartWith())
                                    && zipEntry.getName().endsWith(resourceRule.getEndWith())) {
                                if ((resourceRule.getStartWith().startsWith("!")
                                        && !zipEntry.getName().startsWith(resourceRule.getStartWith().substring(1))
                                        && resourceRule.getEndWith().startsWith("!")
                                        && !zipEntry.getName().endsWith(resourceRule.getEndWith().substring(1)))
                                        ||
                                        (resourceRule.getStartWith().startsWith("!")
                                                && !zipEntry.getName().startsWith(resourceRule.getStartWith().substring(1))
                                                && !resourceRule.getEndWith().startsWith("!")
                                                && zipEntry.getName().endsWith(resourceRule.getEndWith()))
                                        ||
                                        (!resourceRule.getStartWith().startsWith("!")
                                                && zipEntry.getName().startsWith(resourceRule.getStartWith())
                                                && resourceRule.getEndWith().startsWith("!")
                                                && !zipEntry.getName().endsWith(resourceRule.getEndWith().substring(1)))
                                        ||
                                        (!resourceRule.getStartWith().startsWith("!")
                                                && zipEntry.getName().startsWith(resourceRule.getStartWith())
                                                && !resourceRule.getEndWith().startsWith("!")
                                                && zipEntry.getName().endsWith(resourceRule.getEndWith()))) {
                                    adds.add(zipEntry.getName());
                                    zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                                    IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                                    zipOutputStream.closeEntry();
                                }
                            }
                        }
                    }
                    if (SoApis.size() > 0) {
                        JsonArray Apis = new JsonArray();
                        for (String api : SoApis) {
                            Apis.add(api);
                        }
                        rule.put("so_framework", Apis);
                    }
                    zipOutputStream.putNextEntry(new ZipEntry("icon.png"));
                    zipOutputStream.write(AppUtils.drawableToByte(AppUtils.getApkDrawable(Path)));
                    zipOutputStream.closeEntry();
                }
            }
            return new InjectData(
                    cacheFile,
                    packName,
                    cacheFile == null ? (int) FileSize.getFileSize(new File(Path)) / 1024 / 1024 : (int) FileSize.getFileSize(cacheFile) / 1024 / 1024,
                    rule,
                    true,
                    null);
        } catch (Exception e) {
            return new InjectData(
                    null,
                    null,
                    0,
                    null,
                    false,
                    e);
        }
    }

    /**
     * 打包资源文件
     *
     * @param context
     * @param taskInfo
     * @param jks
     * @param signer_type
     */
    public static void ArchiveOld(Activity context, @NotNull TaskInfo taskInfo, File jks, int signer_type) {
        if (!taskInfo.getSrc().exists())
            Toast.makeText(context, R.string.src_faill, Toast.LENGTH_LONG).show();
        else {
            LoadingDialog.getInstance().show(context);
            CloudApp.getCachedThreadPool().execute(() -> {
                try {
                    /**
                     * 脱壳任务
                     */
                    if (isShellTask(taskInfo.getHandle())) {
                        if (isExistsMethod(taskInfo.getOld())) {
                            LoadingDialog.getInstance().hide();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.dialog_tips)
                                        .setMessage(R.string.dialog_tips_fixer)
                                        .setPositiveButton(R.string.no_fixer, (dialog, which) -> {
                                            File file = new File(Environment.getExternalStorageDirectory(),
                                                    taskInfo.getUuid() + ".zip");
                                            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                 FileInputStream stream = new FileInputStream(taskInfo.getOld())) {
                                                int len;
                                                byte[] bytes = new byte[1024 * 1024 * 20];
                                                while ((len = stream.read(bytes)) != -1) {
                                                    fileOutputStream.write(bytes, 0, len);
                                                    fileOutputStream.flush();
                                                }
                                            } catch (Exception e) {
                                                BaleDialog.ShowErrorDialog(context, e);
                                                return;
                                            }
                                            BaleDialog.ShowInfo(context, context.getString(R.string.export_success) + file.getAbsolutePath());
                                        })
                                        .setNegativeButton(R.string.try_fixer, (dialogInterface, i) -> {
                                            LoadingDialog.getInstance().show(context);
                                            CloudApp.getCachedThreadPool().execute(()->{
                                                File cache_dir = new File(context.getCacheDir(), UUID.randomUUID().toString());
                                                File fixer = new File(Environment.getExternalStorageDirectory(), "dump_fixer");
                                                if (!fixer.exists())
                                                    fixer.mkdirs();
                                                if (!cache_dir.exists())
                                                    cache_dir.mkdirs();
                                                try (ZipFile zipFile = new ZipFile(taskInfo.getOld())) {
                                                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                                    while (entries.hasMoreElements()) {
                                                        ZipEntry entry = entries.nextElement();
                                                        if (!entry.isDirectory()) {
                                                            File outFile = new File(cache_dir + File.separator + entry.getName());
                                                            if (!outFile.getParentFile().exists())
                                                                outFile.getParentFile().mkdirs();
                                                            if (!outFile.exists())
                                                                outFile.createNewFile();
                                                            FileOutputStream stream = new FileOutputStream(outFile);
                                                            IOUtils.copy(zipFile.getInputStream(entry), stream);
                                                            stream.flush();
                                                            stream.close();
                                                        }
                                                    }
                                                    Shell.Result result = Shell.sh("dalvikvm -cp "
                                                            + System.getProperty("dexfixer.path")
                                                            + " com.android.dx.unpacker.DexFixer "
                                                            + cache_dir.getAbsolutePath()
                                                            + " "
                                                            + fixer.getAbsolutePath()).exec();
                                                } catch (Exception e) {
                                                    LoadingDialog.getInstance().hide();
                                                    BaleDialog.ShowErrorDialog(context, e);
                                                    return;
                                                } finally {
                                                    LoadingDialog.getInstance().hide();
                                                    try {
                                                        FileUtils.deleteDirectory(cache_dir);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                BaleDialog.ShowInfo(context, context.getString(R.string.export_success) + fixer.getAbsolutePath());
                                            });
                                        })
                                        .show();
                            });
                        } else {
                            File file = new File(Environment.getExternalStorageDirectory(),
                                    taskInfo.getUuid() + ".zip");
                            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                                 FileInputStream stream = new FileInputStream(taskInfo.getOld())) {
                                int len;
                                byte[] bytes = new byte[1024 * 1024 * 20];
                                while ((len = stream.read(bytes)) != -1) {
                                    fileOutputStream.write(bytes, 0, len);
                                    fileOutputStream.flush();
                                }
                            }
                            BaleDialog.ShowInfo(context, context.getString(R.string.export_success) + file.getAbsolutePath());
                        }
                    }
                    /**
                     * 其他任务
                     */
                    else {
                        List<String> NotZipEntry = new ArrayList<>();
                        try (ZipOutputStream Out_Apk = new ZipOutputStream(
                                new FileOutputStream(new File(Environment.getExternalStorageDirectory(),
                                        context.getString(R.string.app_name) + ".apk")));
                             ZipFile Success = new ZipFile(taskInfo.getOld());
                             ZipFile Original = new ZipFile(taskInfo.getSrc())) {
                            Enumeration<? extends ZipEntry> entries = Success.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry zipEntry = entries.nextElement();
                                if (zipEntry.getName().equals("ignore.json")) {
                                    byte[] bytes = StreamUtils.toByte(Success.getInputStream(zipEntry));
                                    NotZipEntry.addAll(new Gson().fromJson(new String(bytes), Ignore.class).getIgnore());
                                } else if (zipEntry.getName().equals("Signer_mode")) {
                                    SignerInfo signerInfo = new Gson().fromJson(new String(StreamUtils.toByte(Success.getInputStream(zipEntry))), SignerInfo.class);
                                    for (SignerInfo.Signer signer : signerInfo.getSigner()) {
                                        try (InputStream inputStream = new FileInputStream(taskInfo.getSrc())) {
                                            ZipEntry entry = new ZipEntry(signer.getPath());
                                            if (signer.getZipMethod() == ZipEntry.STORED){
                                                entry.setSize(inputStream.available());
                                                entry.setCompressedSize(inputStream.available());
                                                InputStream stream = new FileInputStream(taskInfo.getSrc());
                                                CRC32 crc32 = new CRC32();
                                                byte[] bytes = new byte[1024];
                                                int cnt;
                                                while ((cnt = stream.read(bytes)) != -1) {
                                                    crc32.update(bytes, 0, cnt);
                                                }
                                                stream.close();
                                                entry.setCrc(crc32.getValue());
                                            }
                                            entry.setMethod(signer.getZipMethod());
                                            Out_Apk.putNextEntry(entry);
                                            IOUtils.copy(inputStream, Out_Apk);
                                            Out_Apk.closeEntry();
                                            NotZipEntry.add(signer.getPath());
                                        }
                                    }
                                } else {
                                    ZipEntry entry = new ZipEntry(zipEntry.getName());
                                    if (ZipEntry.STORED == zipEntry.getMethod() || entry.getName().equals("resources.arsc")) {
                                        entry.setMethod(ZipEntry.STORED);
                                        entry.setSize(zipEntry.getSize());
                                        entry.setCompressedSize(zipEntry.getCompressedSize());
                                        entry.setCrc(zipEntry.getCrc());
                                    } else
                                        entry.setMethod(ZipEntry.DEFLATED);
                                    Out_Apk.putNextEntry(entry);
                                    IOUtils.copy(Success.getInputStream(zipEntry), Out_Apk);
                                    Out_Apk.closeEntry();
                                    NotZipEntry.add(zipEntry.getName());
                                }
                            }
                            entries = Original.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry zipEntry = entries.nextElement();
                                if (!NotZipEntry.contains(zipEntry.getName())) {
                                    ZipEntry entry = new ZipEntry(zipEntry.getName());
                                    if (ZipEntry.STORED == zipEntry.getMethod()) {
                                        entry.setMethod(ZipEntry.STORED);
                                        entry.setSize(zipEntry.getSize());
                                        entry.setCompressedSize(zipEntry.getCompressedSize());
                                        entry.setCrc(zipEntry.getCrc());
                                    } else
                                        entry.setMethod(ZipEntry.DEFLATED);
                                    Out_Apk.putNextEntry(entry);
                                    IOUtils.copy(Original.getInputStream(zipEntry), Out_Apk);
                                    Out_Apk.closeEntry();
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                            BaleDialog.ShowDialog(context, new File(Environment.getExternalStorageDirectory(),
                                    context.getString(R.string.app_name) + ".apk"));
                        else {
                            KeyInfo keyInfo = new Gson().fromJson(new String(StreamUtils.toByte(new FileInputStream(jks)), StandardCharsets.UTF_8), KeyInfo.class);
                            File signApk = SignerApk.SignApk(new File(Environment.getExternalStorageDirectory(),
                                            context.getString(R.string.app_name) + ".apk"),
                                    new ByteArrayInputStream(Base64.decode(keyInfo.getSigner(), Base64.NO_WRAP)),
                                    keyInfo.getPassWord(),
                                    keyInfo.getAliasPass(),
                                    true,
                                    signer_type);
                            BaleDialog.ShowDialog(context, signApk);
                        }
                    }
                    LoadingDialog.getInstance().hide();
                } catch (Exception e) {
                    e.printStackTrace();
                    BaleDialog.ShowErrorDialog(context, e);
                    LoadingDialog.getInstance().hide();
                }
            });
        }
    }

    /**
     * 校验文件是否是完整的Zip文件
     *
     * @param file
     * @return
     */
    public static boolean isZipFile(@NotNull File file) {
        if (!file.exists())
            return false;
        try (ZipInputStream stream = new ZipInputStream(new FileInputStream(file))) {
            return stream.getNextEntry() != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断资源文件内是否存在需要修复的Method
     *
     * @param file
     * @return
     */
    private static boolean isExistsMethod(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("method") && entry.getName().contains("codeitem") && entry.getName().endsWith(".bin"))
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 判断是否是脱壳任务
     *
     * @param flag
     * @return
     */
    private static boolean isShellTask(long flag) {
        return (flag & 1) != 0 || (flag & 0x200000000L) != 0;
    }
}
