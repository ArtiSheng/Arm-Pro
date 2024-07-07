package armadillo.studio.widget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import java.util.Objects;

import armadillo.studio.R;

public class LoadingDialog {
    private static LoadingDialog loading;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;

    public static LoadingDialog getInstance() {
        if (loading == null) {
            synchronized (LoadingDialog.class) {
                loading = new LoadingDialog();
                return loading;
            }
        }
        return loading;
    }

    @SuppressLint("InflateParams")
    public void show(Context context) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = new AlertDialog.Builder(context)
                .setView(LayoutInflater.from(context).inflate(R.layout.status_loading, null))
                .setCancelable(false)
                .show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showProgress(Context context, String msg) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getString(R.string.dialog_tips));
        progressDialog.setMessage(msg);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void showCancelProgress(Context context, String msg, DialogInterface.OnClickListener listener) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getString(R.string.dialog_tips));
        progressDialog.setMessage(msg);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.cancel), listener);
        progressDialog.show();
    }

    public void showProgress(Context context) {
        showProgress(context, context.getString(R.string.dow_res));
    }

    public void setProgress(int progress) {
        if (progressDialog != null) {
            if (progressDialog.getProgress() != progress)
                progressDialog.setProgress(progress);
        }
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void hide() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog = null;
    }

    public boolean isShowProgress() {
        return progressDialog != null;
    }
}
