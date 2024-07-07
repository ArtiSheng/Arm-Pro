/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import android.annotation.SuppressLint;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import armadillo.studio.R;
import armadillo.studio.model.soft.SoftSingleTrialInfo;

public class TrialInfoAdapter extends BaseQuickAdapter<SoftSingleTrialInfo.data, BaseViewHolder> implements LoadMoreModule {
    public TrialInfoAdapter(int layoutResId, @Nullable List<SoftSingleTrialInfo.data> data) {
        super(layoutResId, data);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull SoftSingleTrialInfo.data data) {
        baseViewHolder.setText(R.id.mac, data.getMac())
                .setText(R.id.count, data.getCount().toString())
                .setText(R.id.time, data.getLastTime());
    }
}
