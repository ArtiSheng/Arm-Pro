/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

import armadillo.studio.R;

public class SignerSeleteAdapter extends BaseQuickAdapter<File, BaseViewHolder> {
    public SignerSeleteAdapter(int layoutResId, @Nullable List<File> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull File file) {
        baseViewHolder.setText(R.id.Name, file.getName().replace(".key", ""));
    }
}