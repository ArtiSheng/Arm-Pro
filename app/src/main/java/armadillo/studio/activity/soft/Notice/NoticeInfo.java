/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Notice;

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
import androidx.appcompat.widget.SwitchCompat;

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
import armadillo.studio.model.soft.SoftNoticeInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnLongClick;

public class NoticeInfo extends BaseActivity<UserSoft.data> {
    @BindView(R.id.notice_title)
    TextInputEditText title;
    @BindView(R.id.notice_msg)
    TextInputEditText msg;
    @BindView(R.id.dialog_title_color)
    TextInputEditText title_color;
    @BindView(R.id.dialog_msg_color)
    TextInputEditText msg_color;
    @BindView(R.id.dialog_confirm_color)
    TextInputEditText confirm_color;
    @BindView(R.id.dialog_cancel_color)
    TextInputEditText cancel_color;
    @BindView(R.id.dialog_additional_color)
    TextInputEditText additional_color;
    @BindView(R.id.notice_confirm_text)
    TextInputEditText confirm_text;
    @BindView(R.id.notice_cancel_text)
    TextInputEditText cancel_text;
    @BindView(R.id.notice_additional_text)
    TextInputEditText additional_text;
    @BindView(R.id.notice_additional_action_body)
    TextInputEditText additional_action_body;
    @BindView(R.id.notice_confirm_action_body)
    TextInputEditText confirm_action_body;
    @BindView(R.id.bj_url)
    TextInputEditText background_url;
    @BindView(R.id.notice_smart_pop)
    SwitchCompat smart_pop;
    @BindView(R.id.notice_dialog_style)
    AppCompatSpinner style;
    @BindView(R.id.notice_cancel)
    AppCompatSpinner cancelable;
    @BindView(R.id.notice_additional_action)
    AppCompatSpinner additional_action;
    @BindView(R.id.notice_confirm_action)
    AppCompatSpinner confirm_action;

    @Override
    protected int BindXML() {
        return R.layout.activity_notice_info;
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

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        style.setSelection(1);
        Loading();
        SocketHelper.UserHelper.GetSoftModelInfo(new SocketCallBack<SoftNoticeInfo>() {
            @Override
            public void next(SoftNoticeInfo body) {
                HideLoading();
                if (body.getCode() == 404) {
                    Toast.makeText(NoticeInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    NoticeInfo.this.finish();
                } else if (body.getData() != null)
                    InitData(body.getData());
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(NoticeInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                NoticeInfo.this.finish();
            }
        }, data.getAppkey(), SoftEnums.Notice);
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
    private void InitData(@NotNull SoftNoticeInfo.data info) {
        title.setText(info.getTitle());
        msg.setText(info.getMsg());
        smart_pop.setChecked(info.getSmartPop());
        style.setSelection(info.getDialogStyle());
        cancelable.setSelection(info.getCancelable() ? 1 : 0);
        title_color.setText(info.getTitleColor().toString());
        msg_color.setText(info.getMsgColor().toString());
        cancel_color.setText(info.getCancelTextColor().toString());
        additional_color.setText(info.getExtraTextColor().toString());
        confirm_color.setText(info.getConfirmTextColor().toString());
        title_color.setTextColor(info.getTitleColor());
        msg_color.setTextColor(info.getMsgColor());
        cancel_color.setTextColor(info.getCancelTextColor());
        additional_color.setTextColor(info.getExtraTextColor());
        confirm_color.setTextColor(info.getConfirmTextColor());
        confirm_text.setText(info.getConfirmText());
        cancel_text.setText(info.getCancelText());
        additional_text.setText(info.getExtraText());
        additional_action.setSelection(info.getExtraAction());
        confirm_action.setSelection(info.getConfirmAction());
        additional_action_body.setText(info.getExtraBody());
        confirm_action_body.setText(info.getConfirmBody());
        background_url.setText(info.getBackgroundUrl());
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
                        || Objects.requireNonNull(additional_color.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                Loading();
                SoftNoticeInfo.data softNoticeInfo = new SoftNoticeInfo.data();
                softNoticeInfo.setTitle(Objects.requireNonNull(title.getText()).toString());
                softNoticeInfo.setMsg(Objects.requireNonNull(msg.getText()).toString());
                softNoticeInfo.setTitleColor(Integer.parseInt(Objects.requireNonNull(title_color.getText()).toString()));
                softNoticeInfo.setMsgColor(Integer.parseInt(Objects.requireNonNull(msg_color.getText()).toString()));
                softNoticeInfo.setConfirmTextColor(Integer.parseInt(Objects.requireNonNull(confirm_color.getText()).toString()));
                softNoticeInfo.setCancelTextColor(Integer.parseInt(Objects.requireNonNull(cancel_color.getText()).toString()));
                softNoticeInfo.setExtraTextColor(Integer.parseInt(Objects.requireNonNull(additional_color.getText()).toString()));
                softNoticeInfo.setConfirmText(Objects.requireNonNull(confirm_text.getText()).toString());
                softNoticeInfo.setCancelText(Objects.requireNonNull(cancel_text.getText()).toString());
                softNoticeInfo.setExtraText(Objects.requireNonNull(additional_text.getText()).toString());
                softNoticeInfo.setConfirmBody(Objects.requireNonNull(confirm_action_body.getText()).toString());
                softNoticeInfo.setExtraBody(Objects.requireNonNull(additional_action_body.getText()).toString());
                softNoticeInfo.setBackgroundUrl(Objects.requireNonNull(background_url.getText()).toString());
                softNoticeInfo.setSmartPop(smart_pop.isChecked());
                softNoticeInfo.setCancelable(cancelable.getSelectedItemPosition() == 1);
                softNoticeInfo.setDialogStyle(style.getSelectedItemPosition());
                softNoticeInfo.setConfirmAction(confirm_action.getSelectedItemPosition());
                softNoticeInfo.setExtraAction(additional_action.getSelectedItemPosition());
                SocketHelper.UserHelper.SaveSoftModelInfo(new SocketCallBack<Basic>() {
                    @Override
                    public void next(Basic body) {
                        HideLoading();
                        Toast.makeText(NoticeInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                        setResult(200);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        HideLoading();
                        Toast.makeText(NoticeInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }, getData().getAppkey(), SoftEnums.Notice, new Gson().toJson(softNoticeInfo));
                return true;
            }
            /**
             * 复制配置
             */
            case R.id.menu_copy: {
                if (Objects.requireNonNull(title_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(msg_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(confirm_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(cancel_color.getText()).toString().isEmpty()
                        || Objects.requireNonNull(additional_color.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                SoftNoticeInfo.data softNoticeInfo = new SoftNoticeInfo.data();
                softNoticeInfo.setTitle(Objects.requireNonNull(title.getText()).toString());
                softNoticeInfo.setMsg(Objects.requireNonNull(msg.getText()).toString());
                softNoticeInfo.setTitleColor(Integer.parseInt(Objects.requireNonNull(title_color.getText()).toString()));
                softNoticeInfo.setMsgColor(Integer.parseInt(Objects.requireNonNull(msg_color.getText()).toString()));
                softNoticeInfo.setConfirmTextColor(Integer.parseInt(Objects.requireNonNull(confirm_color.getText()).toString()));
                softNoticeInfo.setCancelTextColor(Integer.parseInt(Objects.requireNonNull(cancel_color.getText()).toString()));
                softNoticeInfo.setExtraTextColor(Integer.parseInt(Objects.requireNonNull(additional_color.getText()).toString()));
                softNoticeInfo.setConfirmText(Objects.requireNonNull(confirm_text.getText()).toString());
                softNoticeInfo.setCancelText(Objects.requireNonNull(cancel_text.getText()).toString());
                softNoticeInfo.setExtraText(Objects.requireNonNull(additional_text.getText()).toString());
                softNoticeInfo.setConfirmBody(Objects.requireNonNull(confirm_action_body.getText()).toString());
                softNoticeInfo.setExtraBody(Objects.requireNonNull(additional_action_body.getText()).toString());
                softNoticeInfo.setBackgroundUrl(Objects.requireNonNull(background_url.getText()).toString());
                softNoticeInfo.setSmartPop(smart_pop.isChecked());
                softNoticeInfo.setCancelable(cancelable.getSelectedItemPosition() == 1);
                softNoticeInfo.setDialogStyle(style.getSelectedItemPosition());
                softNoticeInfo.setConfirmAction(confirm_action.getSelectedItemPosition());
                softNoticeInfo.setExtraAction(additional_action.getSelectedItemPosition());
                ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", Base64.encodeToString(new Gson().toJson(softNoticeInfo).getBytes(), Base64.NO_WRAP));
                Objects.requireNonNull(cm).setPrimaryClip(mClipData);
                Toast.makeText(this, R.string.copy_success, Toast.LENGTH_LONG).show();
                return true;
            }
            /**
             * 粘贴配置
             */
            case R.id.menu_paste: {
                String paste = PastaUtil.paste();
                if (paste != null) {
                    try {
                        SoftNoticeInfo.data data = new Gson().fromJson(new String(Base64.decode(paste, Base64.NO_WRAP)), SoftNoticeInfo.data.class);
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
                                    additional_color.setText(data.getExtraTextColor().toString());
                                    additional_color.setTextColor(data.getExtraTextColor());
                                    cancel_color.setText(data.getCancelTextColor().toString());
                                    cancel_color.setTextColor(data.getCancelTextColor());
                                    confirm_text.setText(data.getConfirmText());
                                    confirm_action_body.setText(data.getConfirmBody());
                                    confirm_action.setSelection(data.getConfirmAction());
                                    cancel_text.setText(data.getCancelText());
                                    additional_text.setText(data.getExtraText());
                                    additional_action_body.setText(data.getExtraBody());
                                    additional_action.setSelection(data.getExtraAction());
                                    style.setSelection(data.getDialogStyle());
                                    smart_pop.setChecked(data.getSmartPop());
                                    cancelable.setSelection(data.getCancelable() ? 1 : 0);
                                    background_url.setText(data.getBackgroundUrl());
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
