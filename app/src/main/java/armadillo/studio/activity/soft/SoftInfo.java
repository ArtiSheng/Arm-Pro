/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;

import com.lihang.chart.ChartLineItem;
import com.lihang.chart.ChartLineView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.activity.soft.Admob.AdmobInfo;
import armadillo.studio.activity.soft.Custom.CustomInfo;
import armadillo.studio.activity.soft.Notice.NoticeInfo;
import armadillo.studio.activity.soft.Share.ShareInfo;
import armadillo.studio.activity.soft.Single.SingleCardManage;
import armadillo.studio.activity.soft.Single.SingleInfo;
import armadillo.studio.activity.soft.Update.UpdateInfo;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.ChartInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnClick;

public class SoftInfo extends BaseActivity<UserSoft.data> implements CheckBox.OnCheckedChangeListener {
    @BindView(R.id.chartLineView)
    ChartLineView chartLineView;
    @BindView(R.id.soft_mode_notice_switch)
    SwitchCompat notice_switch;
    @BindView(R.id.soft_mode_reg_switch)
    SwitchCompat reg_switch;
    @BindView(R.id.soft_mode_update_switch)
    SwitchCompat update_switch;
    @BindView(R.id.soft_mode_custom_switch)
    SwitchCompat custom_switch;
    @BindView(R.id.soft_mode_admob_switch)
    SwitchCompat admob_switch;
    @BindView(R.id.soft_mode_share_switch)
    SwitchCompat share_switch;
    @BindView(R.id.soft_mode_notice_settings)
    AppCompatImageButton notice_settings;
    @BindView(R.id.soft_mode_reg_settings)
    AppCompatImageButton reg_settings;
    @BindView(R.id.soft_mode_reg_card_manage)
    AppCompatImageButton reg_card_manage;
    @BindView(R.id.soft_mode_update_settings)
    AppCompatImageButton update_settings;
    @BindView(R.id.soft_mode_custom_settings)
    AppCompatImageButton custom_settings;
    @BindView(R.id.soft_mode_admob_settings)
    AppCompatImageButton admob_settings;
    @BindView(R.id.soft_mode_share_settings)
    AppCompatImageButton share_settings;
    private final List<SwitchCompat> handles = new ArrayList<>();
    private final int requestCode = 1000;

    @Override
    protected int BindXML() {
        return R.layout.activity_soft_info;
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({
            R.id.soft_mode_notice_settings,
            R.id.soft_mode_reg_settings,
            R.id.soft_mode_reg_card_manage,
            R.id.soft_mode_update_settings,
            R.id.soft_mode_custom_settings,
            R.id.soft_mode_admob_settings,
            R.id.soft_mode_share_settings
    })
    public void OnClick(@NotNull View view) {
        switch (view.getId()) {
            case R.id.soft_mode_notice_settings: {
                Intent intent = new Intent(this, NoticeInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_reg_settings: {
                Intent intent = new Intent(this, SingleInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_reg_card_manage: {
                Intent intent = new Intent(this, SingleCardManage.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_update_settings: {
                Intent intent = new Intent(this, UpdateInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_custom_settings: {
                Intent intent = new Intent(this, CustomInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_admob_settings: {
                Intent intent = new Intent(this, AdmobInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
            break;
            case R.id.soft_mode_share_settings: {
                Intent intent = new Intent(this, ShareInfo.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", getData());
                intent.putExtras(bundle);
                startActivityForResult(intent, requestCode);
            }
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
        handles.add(notice_switch);
        handles.add(reg_switch);
        handles.add(update_switch);
        handles.add(custom_switch);
        handles.add(admob_switch);
        handles.add(share_switch);
        /**
         * 数据统计
         */
        {
            ArrayList<String> axesTitles = new ArrayList<>();
            ArrayList<Integer> start_count = new ArrayList<>();
            ArrayList<Integer> usr_count = new ArrayList<>();
            start_count.add(0);
            usr_count.add(0);
            for (ChartInfo chartInfo : data.getChartInfos()) {
                axesTitles.add(chartInfo.getTime());
                start_count.add(chartInfo.getStart_count());
                usr_count.add(chartInfo.getUsr_count());
            }
            chartLineView.setHoriItems(axesTitles);
            ArrayList<ChartLineItem> items = new ArrayList<>();
            items.add(new ChartLineItem(usr_count, R.color.red_300, getString(R.string.soft_user_count), true, true));
            items.add(new ChartLineItem(start_count, R.color.black, getString(R.string.soft_start_count), true, true));
            chartLineView.setItems(items);
        }
        /**
         * 初始化功能
         */
        {
            SoftEnums[] flags = SoftEnums.getFlags(data.getHandle());
            for (SoftEnums flag : flags) {
                switch (flag) {
                    case Notice:
                        notice_switch.setChecked(true);
                        break;
                    case SingleVerify:
                        reg_switch.setChecked(true);
                        break;
                    case Update:
                        update_switch.setChecked(true);
                        break;
                    case CustomModule:
                        custom_switch.setChecked(true);
                        break;
                    case Share:
                        share_switch.setChecked(true);
                        break;
                    case Admob:
                        admob_switch.setChecked(true);
                        break;

                }
            }
        }
        notice_switch.setOnCheckedChangeListener(this);
        reg_switch.setOnCheckedChangeListener(this);
        update_switch.setOnCheckedChangeListener(this);
        admob_switch.setOnCheckedChangeListener(this);
        custom_switch.setOnCheckedChangeListener(this);
        share_switch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onError(@NotNull Throwable throwable) {
        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
        finish();
    }

    private void SaveSoftHandle(CompoundButton compoundButton) {
        int handle = 0;
        for (SwitchCompat switchCompat : handles) {
            if (switchCompat.isChecked())
                handle = handle | Objects.requireNonNull(SoftEnums.getFor(switchCompat.getTag().toString())).getType();
        }
        Loading();
        SocketHelper.UserHelper.SaveSoftHandle(new SocketCallBack<Basic>() {
            @Override
            public void next(Basic body) {
                HideLoading();
                Toast.makeText(SoftInfo.this, body.getMsg(), Toast.LENGTH_LONG).show();
                if (body.getCode() != 200)
                    compoundButton.setChecked(!compoundButton.isChecked());
                else
                    setResult(200);
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(SoftInfo.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                compoundButton.setChecked(!compoundButton.isChecked());
            }
        }, getData().getAppkey(), handle);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SaveSoftHandle(buttonView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.requestCode && resultCode == 200)
            setResult(resultCode);
    }
}
