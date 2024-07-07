package armadillo.studio.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.accessibility.TopAccessibility;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class TopServer extends Service {
    @BindView(R.id.clz)
    TextView clzname;
    @BindView(R.id.name)
    TextView name;
    private WindowManager windowManager;
    private RelativeLayout root;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    private WindowManager.LayoutParams layoutParams = null;
    private boolean initViewPlace = false;
    private long exitTime = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void Init() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        root = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        root.setLayoutParams(layoutParams1);
        View view = LayoutInflater.from(this).inflate(R.layout.top_server, root);
        ButterKnife.bind(this, view);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = 0;
        windowManager.addView(root, layoutParams);
        root.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!initViewPlace) {
                        initViewPlace = true;
                        mTouchStartX = event.getRawX();
                        mTouchStartY = event.getRawY();
                        x = event.getRawX();
                        y = event.getRawY();
                    } else {
                        mTouchStartX += (event.getRawX() - x);
                        mTouchStartY += (event.getRawY() - y);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getRawX();
                    y = event.getRawY();
                    updateViewPosition();
                    break;
            }
            return true;
        });
        TopAccessibility.setActivityResult((apk_name, clz) -> {
            name.setText(apk_name);
            clzname.setText(clz);
        });
    }

    @OnClick({R.id.name, R.id.clz})
    public void submit(View view) {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(CloudApp.getContext(), R.string.toast_close_floating, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            windowManager.removeView(root);
            stopSelf();
        }
    }

    private void updateViewPosition() {
        layoutParams.x = (int) (x - mTouchStartX);
        layoutParams.y = (int) (y - mTouchStartY);
        windowManager.updateViewLayout(root, layoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
