/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Update;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.common.utils.PastaUtil;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.SoftUpdateInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnLongClick;

public class UpdateInfo extends BaseActivity<UserSoft.data> {
    @BindView(R.id.update_title)
    TextInputEditText title;
    @BindView(R.id.update_msg)
    TextInputEditText msg;
    @BindView(R.id.dialog_title_color)
    TextInputEditText title_color;
    @BindView(R.id.dialog_msg_color)
    TextInputEditText msg_color;
    @BindView(R.id.dialog_confirm_color)
    TextInputEditText confirm_color;
    @BindView(R.id.dialog_cancel_color)
    TextInputEditText cancel_color;
    @BindView(R.id.dialog_extra_color)
    TextInputEditText extra_color;
    @BindView(R.id.update_confirm_text)
    TextInputEditText confirm_text;
    @BindView(R.id.update_cancel_text)
    TextInputEditText cancel_text;
    @BindView(R.id.update_extra_text)
    TextInputEditText extra_text;
    @BindView(R.id.update_extra_action_body)
    TextInputEditText extra_action_body;
    @BindView(R.id.bj_url)
    TextInputEditText background_url;
    @BindView(R.id.update_url)
    TextInputEditText update_url;
    @BindView(R.id.update_ver)
    TextInputEditText update_ver;
    @BindView(R.id.update_dialog_style)
    AppCompatSpinner style;
    @BindView(R.id.update_cancel)
    AppCompatSpinner cancelable;
    @BindView(R.id.update_extra_action)
    AppCompatSpinner extra_action;
    @BindView(R.id.update_confirm_action)
    AppCompatSpinner confirm_action;

    @Override
    protected int BindXML() {
        return R.layout.activity_update_info;
    }

    @OnFocusChange({
            R.id.dialog_title_color,
            R.id.dialog_msg_color,
            R.id.dialog_confirm_color,
            R.id.dialog_cancel_color,
            R.id.dialog_extra_color
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
            R.id.dialog_extra_color
    })
    public boolean OnLongClick(View view) {
        SeleteColor(view);
        return true;
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void BindData(@NotNull UserSoft.data data) {
        update_ver.setText(data.getVersion().toString());
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        Loading();
        SocketHelper.UserHelper.GetSoftModelInfo(new SocketCallBack<SoftUpdateInfo>() {
            @Override
            public void next(SoftUpdateInfo body) {
                HideLoading();
                if (body.getCode() == 404) {
                    Toast.makeText(UpdateInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    UpdateInfo.this.finish();
                } else if (body.getData() != null)
                    InitData(body.getData());
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(UpdateInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                UpdateInfo.this.finish();
            }
        }, data.getAppkey(), SoftEnums.Update);
    }

    @SuppressLint("SetTextI18n")
    private void InitData(@NotNull SoftUpdateInfo.data info) {
        title.setText(info.getTitle());
        msg.setText(info.getMsg());
        style.setSelection(info.getDialogStyle());
        cancelable.setSelection(info.getCancelable() ? 1 : 0);
        title_color.setText(info.getTitleColor().toString());
        msg_color.setText(info.getMsgColor().toString());
        cancel_color.setText(info.getCancelTextColor().toString());
        extra_color.setText(info.getExtraTextColor().toString());
        confirm_color.setText(info.getConfirmTextColor().toString());
        title_color.setTextColor(info.getTitleColor());
        msg_color.setTextColor(info.getMsgColor());
        cancel_color.setTextColor(info.getCancelTextColor());
        extra_color.setTextColor(info.getExtraTextColor());
        confirm_color.setTextColor(info.getConfirmTextColor());
        confirm_text.setText(info.getConfirmText());
        cancel_text.setText(info.getCancelText());
        extra_text.setText(info.getExtraText());
        extra_action.setSelection(info.getExtraAction());
        confirm_action.setSelection(info.getConfirmAction());
        extra_action_body.setText(info.getExtraBody());
        background_url.setText(info.getBackgroundUrl());
        update_url.setText(info.getUpdateUrl());
        update_ver.setText(info.getUpdateVer().toString());
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /**
             * 保存
             */
            case R.id.menu_save: {
                if (Objects.requireNonNull(title_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(msg_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(confirm_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(cancel_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(extra_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(update_ver.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                Loading();
                SoftUpdateInfo.data softUpdateInfo = new SoftUpdateInfo.data();
                softUpdateInfo.setTitle(Objects.requireNonNull(title.getText()).toString());
                softUpdateInfo.setMsg(Objects.requireNonNull(msg.getText()).toString());
                softUpdateInfo.setTitleColor(Integer.parseInt(Objects.requireNonNull(title_color.getText()).toString()));
                softUpdateInfo.setMsgColor(Integer.parseInt(Objects.requireNonNull(msg_color.getText()).toString()));
                softUpdateInfo.setConfirmTextColor(Integer.parseInt(Objects.requireNonNull(confirm_color.getText()).toString()));
                softUpdateInfo.setCancelTextColor(Integer.parseInt(Objects.requireNonNull(cancel_color.getText()).toString()));
                softUpdateInfo.setExtraTextColor(Integer.parseInt(Objects.requireNonNull(extra_color.getText()).toString()));
                softUpdateInfo.setConfirmText(Objects.requireNonNull(confirm_text.getText()).toString());
                softUpdateInfo.setCancelText(Objects.requireNonNull(cancel_text.getText()).toString());
                softUpdateInfo.setExtraText(Objects.requireNonNull(extra_text.getText()).toString());
                softUpdateInfo.setExtraBody(Objects.requireNonNull(extra_action_body.getText()).toString());
                softUpdateInfo.setBackgroundUrl(Objects.requireNonNull(background_url.getText()).toString());
                softUpdateInfo.setCancelable(cancelable.getSelectedItemPosition() == 1);
                softUpdateInfo.setDialogStyle(style.getSelectedItemPosition());
                softUpdateInfo.setConfirmAction(confirm_action.getSelectedItemPosition());
                softUpdateInfo.setExtraAction(extra_action.getSelectedItemPosition());
                softUpdateInfo.setUpdateUrl(Objects.requireNonNull(update_url.getText()).toString());
                softUpdateInfo.setUpdateVer(Integer.parseInt(Objects.requireNonNull(update_ver.getText()).toString()));
                SocketHelper.UserHelper.SaveSoftModelInfo(new SocketCallBack<Basic>() {
                    @Override
                    public void next(Basic body) {
                        HideLoading();
                        Toast.makeText(UpdateInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                        setResult(200);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        HideLoading();
                        Toast.makeText(UpdateInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }, getData().getAppkey(), SoftEnums.Update, new Gson().toJson(softUpdateInfo));
                return true;
            }
            /**
             * 复制
             */
            case R.id.menu_copy: {
                if (Objects.requireNonNull(title_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(msg_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(confirm_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(cancel_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(extra_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(update_ver.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                SoftUpdateInfo.data softUpdateInfo = new SoftUpdateInfo.data();
                softUpdateInfo.setTitle(Objects.requireNonNull(title.getText()).toString());
                softUpdateInfo.setMsg(Objects.requireNonNull(msg.getText()).toString());
                softUpdateInfo.setTitleColor(Integer.parseInt(Objects.requireNonNull(title_color.getText()).toString()));
                softUpdateInfo.setMsgColor(Integer.parseInt(Objects.requireNonNull(msg_color.getText()).toString()));
                softUpdateInfo.setConfirmTextColor(Integer.parseInt(Objects.requireNonNull(confirm_color.getText()).toString()));
                softUpdateInfo.setCancelTextColor(Integer.parseInt(Objects.requireNonNull(cancel_color.getText()).toString()));
                softUpdateInfo.setExtraTextColor(Integer.parseInt(Objects.requireNonNull(extra_color.getText()).toString()));
                softUpdateInfo.setConfirmText(Objects.requireNonNull(confirm_text.getText()).toString());
                softUpdateInfo.setCancelText(Objects.requireNonNull(cancel_text.getText()).toString());
                softUpdateInfo.setExtraText(Objects.requireNonNull(extra_text.getText()).toString());
                softUpdateInfo.setExtraBody(Objects.requireNonNull(extra_action_body.getText()).toString());
                softUpdateInfo.setBackgroundUrl(Objects.requireNonNull(background_url.getText()).toString());
                softUpdateInfo.setCancelable(cancelable.getSelectedItemPosition() == 1);
                softUpdateInfo.setDialogStyle(style.getSelectedItemPosition());
                softUpdateInfo.setConfirmAction(confirm_action.getSelectedItemPosition());
                softUpdateInfo.setExtraAction(extra_action.getSelectedItemPosition());
                softUpdateInfo.setUpdateUrl(Objects.requireNonNull(update_url.getText()).toString());
                softUpdateInfo.setUpdateVer(Integer.parseInt(Objects.requireNonNull(update_ver.getText()).toString()));
                ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", Base64.encodeToString(new Gson().toJson(softUpdateInfo).getBytes(), Base64.NO_WRAP));
                Objects.requireNonNull(cm).setPrimaryClip(mClipData);
                Toast.makeText(this, R.string.copy_success, Toast.LENGTH_LONG).show();
                return true;
            }
            /**
             * 粘贴
             */
            case R.id.menu_paste: {
                String paste = PastaUtil.paste();
                if (paste != null) {
                    try {
                        SoftUpdateInfo.data data = new Gson().fromJson(new String(Base64.decode(paste, Base64.NO_WRAP)), SoftUpdateInfo.data.class);
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.dialog_import_config)
                                .setNegativeButton(R.string.ok, (dialog, which) -> {
                                    title.setText(data.getTitle());
                                    msg.setText(data.getMsg());
                                    title_color.setText(data.getTitleColor().toString());
                                    title_color.setTextColor(data.getTitleColor());
                                    msg_color.setText(data.getTitleColor().toString());
                                    msg_color.setTextColor(data.getMsgColor());
                                    confirm_color.setText(data.getConfirmTextColor().toString());
                                    confirm_color.setTextColor(data.getConfirmTextColor());
                                    extra_color.setText(data.getExtraTextColor().toString());
                                    extra_color.setTextColor(data.getExtraTextColor());
                                    cancel_color.setText(data.getCancelTextColor().toString());
                                    cancel_color.setTextColor(data.getCancelTextColor());
                                    confirm_text.setText(data.getConfirmText());
                                    confirm_action.setSelection(data.getConfirmAction());
                                    cancel_text.setText(data.getCancelText());
                                    extra_text.setText(data.getExtraText());
                                    extra_action_body.setText(data.getExtraBody());
                                    extra_action.setSelection(data.getExtraAction());
                                    style.setSelection(data.getDialogStyle());
                                    cancelable.setSelection(data.getCancelable() ? 1 : 0);
                                    background_url.setText(data.getBackgroundUrl());
                                    update_url.setText(data.getUpdateUrl());
                                    update_ver.setText(data.getUpdateVer().toString());
                                })
                                .setPositiveButton(R.string.cancel, null)
                                .show();
                    } catch (Exception e) {
                        Toast.makeText(this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
