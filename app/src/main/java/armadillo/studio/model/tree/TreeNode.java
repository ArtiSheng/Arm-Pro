/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.tree;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeNode {
    private String name;
    private boolean isChoose = false;
    private boolean isExpand = false;
    private boolean isClass = false;
    private TreeNode parent;
    private List<TreeNode> child;
    private String tag;

    public TreeNode(String name) {
        this.name = name;
        tag = name;
    }

    public TreeNode(String name, boolean isClass) {
        this.name = name;
        this.isClass = isClass;
        tag = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
        if (!expand) {
            if (child != null)
                for (TreeNode node : child) {
                    node.setExpand(false);
                }
        }
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChild() {
        return child;
    }

    public void setChild(List<TreeNode> child) {
        this.child = child;
    }

    public boolean isClass() {
        return isClass;
    }

    public void setClass(boolean aClass) {
        isClass = aClass;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isChild() {
        return child != null;
    }

    public TreeNode addNode(TreeNode node) {
        if (child == null)
            child = new ArrayList<>();
        node.setParent(this);
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("L");
            List<String> class_type = new ArrayList<>();
            TreeNode treeNode = node;
            do {
                class_type.add(treeNode.getName());
                treeNode = treeNode.getParent();
            } while (!treeNode.isRoot());
            class_type.add(treeNode.getName());
            Collections.reverse(class_type);
            for (int index = 0; index < class_type.size(); index++) {
                if (index == class_type.size() - 1)
                    stringBuilder.append(class_type.get(index)).append(";");
                else
                    stringBuilder.append(class_type.get(index)).append("/");
            }
            node.tag = stringBuilder.toString();
        }
        int index = child.indexOf(node);
        if (index > -1)
            return child.get(index);
        else
            child.add(node);
        return node;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode) {
            return tag.equals(((TreeNode) obj).tag);
//            if (this.getName().equals(((TreeNode) obj).getName()))
//                if (this.getParent() != null)
//                    if (this.getParent().getName().equals(((TreeNode) obj).getParent().getName()))
//                        return true;
//                    else
//                        return false;
//                else
//                    return true;
//            else
//                return false;
        }
        return false;
    }

}
