/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.model.tree.TreeNode;

public class TreeAdapter extends BaseQuickAdapter<TreeNode, BaseViewHolder> {
    private final String TAG = TaskAdapter.class.getSimpleName();

    public TreeAdapter(int layoutResId, @Nullable List<TreeNode> data) {
        super(layoutResId, data);
    }

    public TreeAdapter(int layoutResId) {
        super(layoutResId);
    }

    public interface ICheckBox {
        void onCheckedChanged(int Position, boolean isChecked);
    }

    private ICheckBox iCheckBox;

    public void setOnCheckedChangeListener(ICheckBox iCheckBox) {
        this.iCheckBox = iCheckBox;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, TreeNode treeNode) {
        TreeNode node = treeNode;
        /**
         * 层级
         */
        ((View) baseViewHolder.getView(R.id.icon).getParent()).setPadding(0, 0, 0, 0);
        int i = 1;
        while (node.getParent() != null) {
            ((View) baseViewHolder.getView(R.id.icon).getParent()).setPadding(20 * i, 0, 0, 0);
            node = node.getParent();
            i++;
        }
        baseViewHolder.setText(R.id.name, treeNode.getName());
        if (treeNode.isExpand())
            baseViewHolder.getView(R.id.iv_key).setRotation(45);
        else
            baseViewHolder.getView(R.id.iv_key).setRotation(0);
        MaterialCheckBox checkBox = baseViewHolder.getView(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(treeNode.isChoose());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (iCheckBox != null) {
                if (getWeakRecyclerView().get().isComputingLayout())
                    getWeakRecyclerView().get().post(() -> iCheckBox.onCheckedChanged(getItemPosition(treeNode), isChecked));
                else
                    iCheckBox.onCheckedChanged(getItemPosition(treeNode), isChecked);
            }
        });
        if (treeNode.isClass()) {
            Glide.with(CloudApp.getContext()).load(R.drawable.ic_class_def).into((ImageView) baseViewHolder.getView(R.id.icon));
            baseViewHolder.getView(R.id.iv_key).setVisibility(View.INVISIBLE);
        } else {
            Glide.with(CloudApp.getContext()).load(R.drawable.ic_class_dir).into((ImageView) baseViewHolder.getView(R.id.icon));
            baseViewHolder.getView(R.id.iv_key).setVisibility(View.VISIBLE);
        }
    }
}