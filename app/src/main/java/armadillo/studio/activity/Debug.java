package armadillo.studio.activity;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import butterknife.BindView;
import butterknife.OnClick;

public class Debug extends BaseActivity<String> {
    @BindView(R.id.debug)
    TextView debug;

    @Override
    protected int BindXML() {
        return R.layout.activity_debug;
    }

    @OnClick(R.id.copy)
    public void OnClick(View view) {
        ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", getIntent().getStringExtra("info"));
        Objects.requireNonNull(cm).setPrimaryClip(mClipData);
        Snackbar.make(view, R.string.copy_success, Snackbar.LENGTH_LONG).setAction(R.string.ok, null).show();
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = Objects.requireNonNull(activityManager).getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList)
            appTask.finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void BindData(String data) {
        if (data == null)
            debug.setText(R.string.unknown);
        else
            debug.setText(data);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, R.string.loading_data_error, Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTaskList = Objects.requireNonNull(activityManager).getAppTasks();
            for (ActivityManager.AppTask appTask : appTaskList)
                appTask.finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
