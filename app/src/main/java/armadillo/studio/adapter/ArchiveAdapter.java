/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package armadillo.studio.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import armadillo.studio.R;

public class ArchiveAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public ArchiveAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull String s) {
        Glide.with(getContext()).load((s.contains("失败") || s.contains("failed")) ? R.drawable.ic_close : R.drawable.ic_tick).into((ImageView) baseViewHolder.getView(R.id.state_img));
        baseViewHolder.setText(R.id.state, s);
        baseViewHolder.getView(R.id.state).setSelected(true);
    }
}
