/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.activity.soft.Admob;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.AdmobEnums;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.common.utils.PastaUtil;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.SoftAdmobInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;

public class AdmobInfo extends BaseActivity<UserSoft.data> {
    @BindView(R.id.admob_banner_switch)
    SwitchCompat banner_switch;
    @BindView(R.id.admob_interstitial_switch)
    SwitchCompat interstitial_switch;
    @BindView(R.id.admob_rewarded_switch)
    SwitchCompat rewarded_switch;
    @BindView(R.id.admob_banner_ids)
    EditText banner_rules;
    @BindView(R.id.admob_interstitial_ids)
    EditText interstitial_rules;
    @BindView(R.id.admob_rewarded_ids)
    EditText rewarded_rules;
    @BindView(R.id.admob_rules)
    EditText admob_rules;
    private final List<SwitchCompat> handles = new ArrayList<>();

    @Override
    protected int BindXML() {
        return R.layout.activity_admob_info;
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        handles.add(banner_switch);
        handles.add(interstitial_switch);
        handles.add(rewarded_switch);
        Loading();
        SocketHelper.UserHelper.GetSoftModelInfo(new SocketCallBack<SoftAdmobInfo>() {
            @Override
            public void next(SoftAdmobInfo body) {
                HideLoading();
                if (body.getCode() == 404) {
                    Toast.makeText(AdmobInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    AdmobInfo.this.finish();
                } else if (body.getData() != null)
                    InitData(body.getData());
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(AdmobInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                AdmobInfo.this.finish();
            }
        }, data.getAppkey(), SoftEnums.Admob);
    }

    private void InitData(@NotNull SoftAdmobInfo.data data) {
        for (AdmobEnums flag : AdmobEnums.getFlags(data.getHandle())) {
            switch (flag) {
                case Banner:
                    banner_switch.setChecked(true);
                    break;
                case rewarded:
                    rewarded_switch.setChecked(true);
                    break;
                case interstitial:
                    interstitial_switch.setChecked(true);
                    break;
            }
        }
        banner_rules.setText(data.getBannerIds());
        interstitial_rules.setText(data.getInterstitialIds());
        rewarded_rules.setText(data.getRewardedIds());
        admob_rules.setText(data.getRules());
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
                if (Objects.requireNonNull(admob_rules.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                Loading();
                SoftAdmobInfo.data softAdmobInfo = new SoftAdmobInfo.data();
                softAdmobInfo.setBannerIds(banner_rules.getText().toString());
                softAdmobInfo.setInterstitialIds(interstitial_rules.getText().toString());
                softAdmobInfo.setRewardedIds(rewarded_rules.getText().toString());
                softAdmobInfo.setRules(admob_rules.getText().toString());
                int handle = 0;
                for (SwitchCompat compat : handles)
                    if (compat.isChecked())
                        handle = handle | Objects.requireNonNull(AdmobEnums.getFor(compat.getTag().toString())).getType();
                softAdmobInfo.setHandle(handle);
                softAdmobInfo.setOpenIds("");
                SocketHelper.UserHelper.SaveSoftModelInfo(new SocketCallBack<Basic>() {
                    @Override
                    public void next(Basic body) {
                        HideLoading();
                        Toast.makeText(AdmobInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                        setResult(200);
                    }

                    @Override
                    public void error(Throwable throwable) {
                        HideLoading();
                        Toast.makeText(AdmobInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }, getData().getAppkey(), SoftEnums.Admob, new Gson().toJson(softAdmobInfo));
                return true;
            }
            /**
             * 复制配置
             */
            case R.id.menu_copy: {
                if (Objects.requireNonNull(admob_rules.getText()).toString().isEmpty()) {
                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                    return true;
                }
                SoftAdmobInfo.data softAdmobInfo = new SoftAdmobInfo.data();
                softAdmobInfo.setBannerIds(banner_rules.getText().toString());
                softAdmobInfo.setInterstitialIds(interstitial_rules.getText().toString());
                softAdmobInfo.setRewardedIds(rewarded_rules.getText().toString());
                softAdmobInfo.setRules(admob_rules.getText().toString());
                int handle = 0;
                for (SwitchCompat compat : handles)
                    if (compat.isChecked())
                        handle = handle | Objects.requireNonNull(AdmobEnums.getFor(compat.getTag().toString())).getType();
                softAdmobInfo.setHandle(handle);
                softAdmobInfo.setOpenIds("");
                ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", Base64.encodeToString(new Gson().toJson(softAdmobInfo).getBytes(), Base64.NO_WRAP));
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
                        SoftAdmobInfo.data data = new Gson().fromJson(new String(Base64.decode(paste, Base64.NO_WRAP)), SoftAdmobInfo.data.class);
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.dialog_import_config)
                                .setNegativeButton(R.string.ok, (dialog, which) -> {
                                    for (AdmobEnums flag : AdmobEnums.getFlags(data.getHandle())) {
                                        switch (flag) {
                                            case Banner:
                                                banner_switch.setChecked(true);
                                                break;
                                            case rewarded:
                                                rewarded_switch.setChecked(true);
                                                break;
                                            case interstitial:
                                                interstitial_switch.setChecked(true);
                                                break;
                                        }
                                    }
                                    banner_rules.setText(data.getBannerIds());
                                    interstitial_rules.setText(data.getInterstitialIds());
                                    rewarded_rules.setText(data.getRewardedIds());
                                    admob_rules.setText(data.getRules());
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
