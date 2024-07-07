/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.util.Log;
import android.widget.CheckBox;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import armadillo.studio.R;
import armadillo.studio.adapter.TreeAdapter;
import armadillo.studio.model.tree.TreeNode;

public class TreeHelper {
    private final static String TAG = TreeHelper.class.getSimpleName();

    public static void CloseNode(@NotNull TreeNode node, List<TreeNode> list) {
        /**
         * 判断是否有子节点
         */
        if (node.isChild()) {
            /**
             * 获取所有子节点
             */
            for (TreeNode treeNode : node.getChild()) {
                /**
                 * 判断上层节点是否展开
                 */
                if (treeNode.getParent().isExpand()) {
                    /**
                     * 添加需要移除的子节点到List
                     */
                    list.add(treeNode);
                    if (treeNode.isChild())
                    /**
                     * 遍历下层子节点
                     */
                        CloseNode(treeNode, list);
                }
            }
        }
    }

    public static void ChooseNode(TreeAdapter class_adapter, List<TreeNode> root, @NotNull TreeNode node, boolean isCheck) {
        Log.e(TAG, "修改选中状态 " + node.getName() + " " + isCheck);
        if (node.isChild()) {
            node.setChoose(isCheck);
            for (TreeNode treeNode : node.getChild()) {
                treeNode.setChoose(isCheck);
                int position = root.indexOf(treeNode);
                if (position != -1) {
                    CheckBox checkBox = (CheckBox) class_adapter.getViewByPosition(position, R.id.checkBox);
                    if (checkBox != null)
                        checkBox.setChecked(isCheck);
                }
                ChooseNode(class_adapter, root, treeNode, isCheck);
            }
        } else
            node.setChoose(isCheck);
    }

    public static void GetSeleteNode(@NotNull List<TreeNode> root, HashSet<String> list) {
        for (TreeNode node : root) {
            if (node.isChoose()) {
                if (node.isChild())
                    GetSeleteNode(node.getChild(), list);
                else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("L");
                    TreeNode treeNode = node;
                    List<String> class_type = new ArrayList<>();
                    do {
                        class_type.add(treeNode.getName());
                        treeNode = treeNode.getParent();
                    } while (!treeNode.isRoot());
                    Collections.reverse(class_type);
                    for (int index = 0; index < class_type.size(); index++) {
                        if (index == class_type.size() - 1)
                            stringBuilder.append(class_type.get(index)).append(";");
                        else
                            stringBuilder.append(class_type.get(index)).append("/");
                    }
                    list.add(stringBuilder.toString());
                }
            }
        }
    }
}
