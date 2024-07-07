/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jf.dexlib2.iface.ClassDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import armadillo.studio.common.base.callback.TreeNodeCall;
import armadillo.studio.model.tree.TreeNode;

public class Tree {
    private final List<TreeNode> treeNodes = new ArrayList<>();
    private final String TAG = Tree.class.getSimpleName();

    public Tree(@NotNull HashMap<String, HashSet<ClassDef>> classDefs, @NotNull TreeNodeCall<List<TreeNode>> call) {
        call.Loading();
        long start = System.currentTimeMillis();
        for (Map.Entry<String, HashSet<ClassDef>> entry : classDefs.entrySet()) {
            TreeNode root = new TreeNode(entry.getKey().replace(".dex", ""));
            treeNodes.add(root);
            for (ClassDef classDef : entry.getValue()) {
                String class_type = classDef.getType().substring(1, classDef.getType().length() - 1);
                String[] split = class_type.split("/");
                TreeNode tree = root;
                for (int index = 0; index < split.length; index++) {
                    TreeNode treeNode = new TreeNode(split[index], index + 1 == split.length);
                    tree = tree.addNode(treeNode);
                }
            }
            long Sort_start = System.currentTimeMillis();
            Sort(root);
            Log.e(TAG, String.format("解析Class数量 %d", entry.getValue().size()));
            Log.e(TAG, String.format("排序耗时%.2f", (float) (System.currentTimeMillis() - Sort_start) / 1000));
        }
        Collections.sort(treeNodes, (o1, o2) -> {
            Integer f = f(o1.getName());
            Integer f2 = f(o2.getName());
            return Integer.compare(f, f2);
        });
        Log.e(TAG, String.format("解析耗时%.2f", (float) (System.currentTimeMillis() - start) / 1000));
        call.BindData(treeNodes);
    }

    private void Sort(@NotNull TreeNode treeNode) {
        if (treeNode.isChild()) {
            Collections.sort(treeNode.getChild(), (o1, o2) -> {
                if (o1.isChild() && o2.isClass())
                    return -1;
                if (o1.isClass() && o2.isChild())
                    return 1;
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            });
            for (TreeNode node : treeNode.getChild()) {
                if (node.isChild())
                    Sort(node);
            }
        }
    }

    @NotNull
    private static Integer f(@NotNull String filename) {
        char[] cs = filename.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : cs) {
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        if (builder.toString().isEmpty())
            return 0;
        return Integer.parseInt(builder.toString());
    }
}