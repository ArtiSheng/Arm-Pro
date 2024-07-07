/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import armadillo.studio.R;

public class ActivityAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public ActivityAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull String name) {
        baseViewHolder.setText(R.id.Name, name);
    }
}