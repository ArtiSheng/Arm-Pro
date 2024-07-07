/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Single;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.SoftSingleInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnLongClick;

public class SingleInfo extends BaseActivity<UserSoft.data> {
    @BindView(R.id.single_title)
    TextInputEditText title;
    @BindView(R.id.single_msg)
    TextInputEditText msg;
    @BindView(R.id.try_count)
    TextInputEditText try_count;
    @BindView(R.id.try_minutes)
    TextInputEditText try_minutes;
    @BindView(R.id.dialog_title_color)
    TextInputEditText title_color;
    @BindView(R.id.dialog_msg_color)
    TextInputEditText msg_color;
    @BindView(R.id.dialog_confirm_color)
    TextInputEditText confirm_color;
    @BindView(R.id.dialog_additional_color)
    TextInputEditText extry_color;
    @BindView(R.id.dialog_cancel_color)
    TextInputEditText cancel_color;
    @BindView(R.id.weburl)
    TextInputEditText web_url;
    @BindView(R.id.bj_url)
    TextInputEditText background_url;
    @BindView(R.id.dialog_confirm_text)
    TextInputEditText confirm_text;
    @BindView(R.id.dialog_cancel_text)
    TextInputEditText cancel_text;
    @BindView(R.id.dialog_extry_text)
    TextInputEditText extry_text;
    @BindView(R.id.dialog_style)
    AppCompatSpinner style;
    @BindView(R.id.bind_mode)
    AppCompatSpinner bind_mode;
    @BindView(R.id.dialog_extry_action)
    AppCompatSpinner extry_action;

    @Override
    protected int BindXML() {
        return R.layout.activity_single_info;
    }

    @OnFocusChange({
            R.id.dialog_title_color,
            R.id.dialog_msg_color,
            R.id.dialog_confirm_color,
            R.id.dialog_cancel_color,
            R.id.dialog_additional_color
    })
    public void OnFocusChange(View view, boolean b) {
        if (b)
            SeleteColor(view);
    }

    @OnLongClick({
            R.id.dialog_title_color,
            R.id.dialog_msg_color,
            R.id.dialog_confirm_color,
            R.id.dialog_cancel_color,
            R.id.dialog_additional_color
    })
    public boolean OnLongClick(View view) {
        SeleteColor(view);
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void InitData(@NotNull SoftSingleInfo.data info) {
        title.setText(info.getTitle());
        msg.setText(info.getMsg());
        try_count.setText(info.getTryCount().toString());
        try_minutes.setText(info.getTryMinutes().toString());
        title_color.setText(info.getTitleTextColor().toString());
        msg_color.setText(info.getMsgTextColor().toString());
        confirm_color.setText(info.getConfirmTextColor().toString());
        extry_color.setText(info.getExtraTextColor().toString());
        cancel_color.setText(info.getCancelTextColor().toString());
        web_url.setText(info.getWeburl());
        background_url.setText(info.getBackgroundUrl());
        confirm_text.setText(info.getConfirmText());
        cancel_text.setText(info.getCancelText());
        extry_text.setText(info.getExtraText());
        style.setSelection(info.getDialogStyle());
        bind_mode.setSelection(info.getBindMode());
        extry_action.setSelection(info.getExtraAction());
        title_color.setTextColor(info.getTitleTextColor());
        msg_color.setTextColor(info.getMsgTextColor());
        cancel_color.setTextColor(info.getCancelTextColor());
        extry_color.setTextColor(info.getExtraTextColor());
        confirm_color.setTextColor(info.getConfirmTextColor());
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        Loading();
        SocketHelper.UserHelper.GetSoftModelInfo(new SocketCallBack<SoftSingleInfo>() {
            @Override
            public void next(SoftSingleInfo body) {
                HideLoading();
                if (body.getCode() == 404) {
                    Toast.makeText(SingleInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    SingleInfo.this.finish();
                } else if (body.getData() != null) {
                    InitData(body.getData());
                }
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(SingleInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                SingleInfo.this.finish();
            }
        }, data.getAppkey(), SoftEnums.SingleVerify);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_verify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save: {
                if (Objects.requireNonNull(try_count.getText()).toString().isEmpty()
                        || Objects.requireNonNull(try_minutes.getText()).toString().isEmpty()
                        || Objects.requireNonNull(title_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(msg_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(confirm_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(cancel_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(extry_color.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                Loading();
                SoftSingleInfo.data softSingleInfo = new SoftSingleInfo.data();
                softSingleInfo.setTitle(Objects.requireNonNull(title.getText()).toString());
                softSingleInfo.setMsg(Objects.requireNonNull(msg.getText()).toString());
                softSingleInfo.setTryCount(Integer.parseInt(Objects.requireNonNull(try_count.getText()).toString()));
                softSingleInfo.setTryMinutes(Integer.parseInt(Objects.requireNonNull(try_minutes.getText()).toString()));
                softSingleInfo.setTitleTextColor(Integer.parseInt(Objects.requireNonNull(title_color.getText()).toString()));
                softSingleInfo.setMsgTextColor(Integer.parseInt(Objects.requireNonNull(msg_color.getText()).toString()));
                softSingleInfo.setConfirmTextColor(Integer.parseInt(Objects.requireNonNull(confirm_color.getText()).toString()));
                softSingleInfo.setCancelTextColor(Integer.parseInt(Objects.requireNonNull(cancel_color.getText()).toString()));
                softSingleInfo.setExtraTextColor(Integer.parseInt(Objects.requireNonNull(extry_color.getText()).toString()));
                softSingleInfo.setConfirmText(Objects.requireNonNull(confirm_text.getText()).toString());
                softSingleInfo.setCancelText(Objects.requireNonNull(cancel_text.getText()).toString());
                softSingleInfo.setExtraText(Objects.requireNonNull(extry_text.getText()).toString());
                softSingleInfo.setWeburl(Objects.requireNonNull(web_url.getText()).toString());
                softSingleInfo.setBackgroundUrl(Objects.requireNonNull(background_url.getText()).toString());
                softSingleInfo.setDialogStyle(style.getSelectedItemPosition());
                softSingleInfo.setBindMode(bind_mode.getSelectedItemPosition());
                softSingleInfo.setExtraAction(extry_action.getSelectedItemPosition());
                SocketHelper.UserHelper.SaveSoftModelInfo(new SocketCallBack<Basic>() {
                    @Override
                    public void next(Basic body) {
                        HideLoading();
                        Toast.makeText(SingleInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                        setResult(200);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        HideLoading();
                        Toast.makeText(SingleInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }, getData().getAppkey(), SoftEnums.SingleVerify, new Gson().toJson(softSingleInfo));
            }
            return true;
            case R.id.menu_try: {
                Intent intent = new Intent(this, SingleTrialManage.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivity(intent);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
