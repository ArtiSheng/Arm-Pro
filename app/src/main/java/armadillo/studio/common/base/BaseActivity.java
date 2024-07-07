/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.common.base.view.IViewCall;
import armadillo.studio.widget.LoadingDialog;
import butterknife.ButterKnife;

public abstract class BaseActivity<V> extends AppCompatActivity implements IViewCall<V> {
    public final static String TAG = BaseActivity.class.getSimpleName();

    /**
     * 绑定XML
     *
     * @return ResId
     */
    protected abstract int BindXML();

    /**
     * 自动加载Intent Data数据
     *
     * @return true自动加载false不加载
     */
    protected abstract boolean AutoLoadData();

    public V data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(BindXML());
        Transition slide = new Slide(Gravity.START);
        slide.setDuration(300);
        slide.setInterpolator(new OvershootInterpolator());
        slide.excludeTarget(android.R.id.statusBarBackground, true);
        slide.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        }
        ButterKnife.bind(this);
        if (AutoLoadData()) {
            if (getIntent().getExtras() == null)
                BindData(null);
            else {
                data = (V) getIntent().getSerializableExtra("data");
                if (data == null)
                    data = (V) getIntent().getExtras().get("data");
                if (data != null)
                    BindData(data);
                else
                    onError(new NullPointerException("Data Null"));
            }
        } else
            BindData(null);
    }

    public <T extends View> T BindView(@IdRes int id) {
        return getDelegate().findViewById(id);
    }

    @Override
    public void Loading() {
        LoadingDialog.getInstance().show(this);
    }

    @Override
    public void HideLoading() {
        LoadingDialog.getInstance().hide();
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public V getData() {
        return data;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void SeleteColor(View view) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle(getString(R.string.dialog_selete_color))
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                    ((TextInputEditText) view).setText(String.valueOf(selectedColor));
                    ((TextInputEditText) view).setTextColor(selectedColor);
                })
                .setNegativeButton(R.string.cancel, null)
                .build()
                .show();
    }

}
