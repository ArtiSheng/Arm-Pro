/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Custom;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import armadillo.studio.model.soft.SoftCustomInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class CustomInfo extends BaseActivity<UserSoft.data> {
    @BindView(R.id.custom_loader_mode)
    AppCompatSpinner custom_loader_mode;
    @BindView(R.id.custom_invoke_mode)
    AppCompatSpinner custom_invoke_mode;
    @BindView(R.id.custom_loader_path)
    TextInputEditText custom_loader_path;
    @BindView(R.id.custom_invoke_rule)
    EditText custom_invoke_rule;

    @Override
    protected int BindXML() {
        return R.layout.activity_custom_info;
    }

    @OnClick(R.id.custom_helper)
    public void OnClick(View view) {
        new AlertDialog.Builder(this)
                .setView(R.layout.dialog_custom_helper)
                .setPositiveButton(R.string.cancel, null)
                .show();
    }

    @OnItemSelected(R.id.custom_loader_mode)
    public void OnItemSelected(int position) {
        switch (position) {
            case 0:
                custom_loader_path.setHint("/assets/test.jar");
                break;
            case 1:
                custom_loader_path.setHint("[sdcard]/test.jar");
                break;
            case 2:
                custom_loader_path.setHint("http://www.google.com/test.jar");
                break;
        }
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        Loading();
        SocketHelper.UserHelper.GetSoftModelInfo(new SocketCallBack<SoftCustomInfo>() {
            @Override
            public void next(SoftCustomInfo body) {
                HideLoading();
                if (body.getCode() == 404) {
                    Toast.makeText(CustomInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    CustomInfo.this.finish();
                } else if (body.getData() != null)
                    InitData(body.getData());
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(CustomInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                CustomInfo.this.finish();
            }
        }, data.getAppkey(), SoftEnums.CustomModule);
    }

    private void InitData(@NotNull SoftCustomInfo.data data) {
        custom_loader_mode.setSelection(data.getCustomLoaderMode());
        custom_loader_path.setText(data.getCustomLoaderPath());
        custom_invoke_mode.setSelection(data.getCustomInvokeMode());
        custom_invoke_rule.setText(data.getCustomInvokeRule());
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
                if (Objects.requireNonNull(custom_loader_path.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                Loading();
                SoftCustomInfo.data softCustomInfo = new SoftCustomInfo.data();
                softCustomInfo.setCustomLoaderMode(custom_loader_mode.getSelectedItemPosition());
                softCustomInfo.setCustomLoaderPath(Objects.requireNonNull(custom_loader_path.getText()).toString());
                softCustomInfo.setCustomInvokeMode(custom_invoke_mode.getSelectedItemPosition());
                softCustomInfo.setCustomInvokeRule(Objects.requireNonNull(custom_invoke_rule.getText()).toString());
                SocketHelper.UserHelper.SaveSoftModelInfo(new SocketCallBack<Basic>() {
                    @Override
                    public void next(Basic body) {
                        HideLoading();
                        Toast.makeText(CustomInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                        setResult(200);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        HideLoading();
                        Toast.makeText(CustomInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }, getData().getAppkey(), SoftEnums.CustomModule, new Gson().toJson(softCustomInfo));
                return true;
            }
            /**
             * 复制配置
             */
            case R.id.menu_copy: {
                if (Objects.requireNonNull(custom_loader_path.getText()).toString().isEmpty()
                        || Objects.requireNonNull(custom_invoke_rule.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                SoftCustomInfo.data softCustomInfo = new SoftCustomInfo.data();
                softCustomInfo.setCustomLoaderMode(custom_loader_mode.getSelectedItemPosition());
                softCustomInfo.setCustomLoaderPath(Objects.requireNonNull(custom_loader_path.getText()).toString());
                softCustomInfo.setCustomInvokeMode(custom_invoke_mode.getSelectedItemPosition());
                softCustomInfo.setCustomInvokeRule(Objects.requireNonNull(custom_invoke_rule.getText()).toString());
                ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", Base64.encodeToString(new Gson().toJson(softCustomInfo).getBytes(), Base64.NO_WRAP));
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
                        SoftCustomInfo.data data = new Gson().fromJson(new String(Base64.decode(paste, Base64.NO_WRAP)), SoftCustomInfo.data.class);
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.dialog_import_config)
                                .setNegativeButton(R.string.ok, (dialog, which) -> {
                                    custom_loader_mode.setSelection(data.getCustomLoaderMode());
                                    custom_loader_path.setText(data.getCustomLoaderPath());
                                    custom_invoke_mode.setSelection(data.getCustomInvokeMode());
                                    custom_invoke_rule.setText(data.getCustomInvokeRule());
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
