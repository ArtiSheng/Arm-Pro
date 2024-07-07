/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package armadillo.studio.widget;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.File;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.jks.SignerApk;


public class BaleDialog {
    public static void ShowDialog(Context context, File file) {
        if (file == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ShowError(context, "File Null.....");
            } else
                new Handler(Looper.getMainLooper()).post(() -> {
                    ShowError(context, "File Null.....");
                });
        } else {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Show(context, context.getString(R.string.archive_succecc) + file.getAbsolutePath(), file);
            } else
                new Handler(Looper.getMainLooper()).post(() -> {
                    Show(context, context.getString(R.string.archive_succecc) + file.getAbsolutePath(), file);
                });
        }
    }

    public static void ShowErrorDialog(Context context, Throwable throwable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ShowError(context, String.format(context.getString(R.string.exception), Log.getStackTraceString(throwable)));
        } else
            new Handler(Looper.getMainLooper()).post(() -> {
                ShowError(context, String.format(context.getString(R.string.exception), Log.getStackTraceString(throwable)));
            });
    }

    public static void ShowErrorDialog(Context context, String throwable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ShowError(context, throwable);
        } else
            new Handler(Looper.getMainLooper()).post(() -> {
                ShowError(context, throwable);
            });
    }

    private static void Show(Context context, String msg) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_tips)
                .setMessage(msg)
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(R.string.install, (dialogInterface, i) -> {
                    AppUtils.installApk(context,
                            Environment.getExternalStorageDirectory() +
                                    File.separator +
                                    context.getString(R.string.app_name) + "_Sign.apk");
                })
                .show();
    }

    private static void Show(Context context, String msg, File out) {
        AlertDialog install = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_tips)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.install, null)
                .show();
        install.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(view2 -> {
            PackageInfo info = AppUtils.GetPackageInfo(out.getAbsolutePath());
            LoadingDialog.getInstance().show(context);
            CloudApp.getCachedThreadPool().execute(()->{
                if (AppUtils.isInstalled(context, info.packageName)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        LoadingDialog.getInstance().hide();
                        if (!SignerApk.getSignMd5(out).equals(SignerApk.getSignMd5(info.packageName))) {
                            AlertDialog dialog = new AlertDialog.Builder(context)
                                    .setTitle(R.string.dialog_tips)
                                    .setMessage(R.string.signer_fall)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, null)
                                    .show();
                            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_DELETE);
                                intent.setData(Uri.parse("package:" + info.packageName));
                                context.startActivity(intent);
                                dialog.dismiss();
                            });
                        } else
                            AppUtils.installApk(context,
                                    out.getAbsolutePath());
                    });
                } else
                    new Handler(Looper.getMainLooper()).post(() -> {
                        LoadingDialog.getInstance().hide();
                        AppUtils.installApk(context,
                                out.getAbsolutePath());
                    });
            });
        });
    }

    public static void ShowInfo(Context context, String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(msg)
                    .setPositiveButton(R.string.cancel, null)
                    .show();
        } else
            new Handler(Looper.getMainLooper()).post(() -> {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_tips)
                        .setMessage(msg)
                        .setPositiveButton(R.string.cancel, null)
                        .show();
            });
    }

    private static void ShowError(Context context, String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(msg)
                    .setPositiveButton(R.string.cancel, null)
                    .show();
        } else
            new AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(msg)
                    .setPositiveButton(R.string.cancel, null)
                    .show();
    }
}
