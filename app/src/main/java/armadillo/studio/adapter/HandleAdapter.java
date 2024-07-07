/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.model.handle.HandleNode;
import armadillo.studio.model.handle.Node;


public class HandleAdapter<T> extends BaseQuickAdapter<T, BaseViewHolder> {
    private final String TAG = HandleAdapter.class.getName();
    private final HashSet<Node> selete = new HashSet<>();
    private int SingleIndex = -1;

    public HashSet<Node> getSelete() {
        return selete;
    }

    public int getSingleIndex() {
        return SingleIndex;
    }

    public void setSingleIndex(int singleIndex) {
        SingleIndex = singleIndex;
    }

    public int getNodePosition(Node node) {
        return getItemPosition((T) node);
    }

    public HandleAdapter(int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, T t) {
        if (t instanceof HandleNode) {
            HandleNode handleNode = (HandleNode) t;
            baseViewHolder.setText(R.id.name, handleNode.getName())
                    .setGone(R.id.avatar, true)
                    .setGone(R.id.desc, true)
                    .setGone(R.id.checked, true)
                    .setGone(R.id.radio, true);
            TextView view = baseViewHolder.getView(R.id.name);
            view.setCompoundDrawables(null, null, null, null);
        } else if (t instanceof Node) {
            Node node = (Node) t;
            baseViewHolder.setText(R.id.name, node.getName())
                    .setVisible(R.id.avatar, true)
                    .setVisible(R.id.checked, true)
                    .setVisible(R.id.desc, true)
                    .setGone(R.id.checked, node.getParent().isSingle())
                    .setGone(R.id.radio, !node.getParent().isSingle())
                    .setText(R.id.desc, node.getDesc());
            TextView view = baseViewHolder.getView(R.id.name);

            view.setCompoundDrawables(null, null, getVip(node.getVip()), null);
            view.setCompoundDrawablePadding(10);
            Glide.with(CloudApp.getContext()).load(node.getIcon()).into((ImageView) baseViewHolder.getView(R.id.avatar));
            if (node.getParent().isSingle()) {
                RadioButton radio = baseViewHolder.getView(R.id.radio);
                radio.setChecked(node.isSelected());
                if (node.isSelected())
                    SingleIndex = getNodePosition(node);
            } else {
                CheckBox checkBox = baseViewHolder.getView(R.id.checked);
                checkBox.setChecked(selete.contains(node));
                if (checkBox.isChecked())
                    SingleIndex = -1;
            }
        }
    }

    @Nullable
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getVip(int vip) {
        if (vip > 0) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_vip, getContext().getTheme());
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            return drawable;
        } else
            return null;
    }
}
