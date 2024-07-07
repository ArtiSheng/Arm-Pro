/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.ui.software.SoftwareLiveDate;
import armadillo.studio.common.utils.GlideRoundTransform;

public class SoftwareAdapter extends BaseQuickAdapter<SoftwareLiveDate, BaseViewHolder> implements LoadMoreModule {

    public SoftwareAdapter(int layoutResId, @Nullable List<SoftwareLiveDate> data) {
        super(layoutResId, data);
    }

    public SoftwareAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull SoftwareLiveDate data) {
        baseViewHolder.setText(R.id.name, data.getData().getName() + " · " + data.getData().getVersion())
                .setText(R.id.packagename, data.getData().getPackageName())
                .setText(R.id.soft_use_count, data.getData().getTotal_user().toString());
        if (data.getData().getHandle() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            SoftEnums[] flags = SoftEnums.getFlags(data.getData().getHandle());
            for (int i = 0; i < flags.length; i++) {
                if (i == flags.length - 1)
                    stringBuilder.append(flags[i].getDesc());
                else
                    stringBuilder.append(flags[i].getDesc()).append("|");
            }
            baseViewHolder.setText(R.id.model, String.format(CloudApp.getContext().getString(R.string.soft_mode), stringBuilder.toString()));
        } else
            baseViewHolder.setText(R.id.model, String.format(CloudApp.getContext().getString(R.string.soft_mode), CloudApp.getContext().getString(R.string.soft_mode_null)));
        Glide.with(CloudApp.getContext())
                .load("http://" + CloudApp.getContext().getResources().getString(R.string.host) + ":8000/get?key=" + data.getData().getAppkey())
                .transform(new CenterCrop(), new GlideRoundTransform())
                .into((ImageView) baseViewHolder.getView(R.id.avatar));
    }
}
