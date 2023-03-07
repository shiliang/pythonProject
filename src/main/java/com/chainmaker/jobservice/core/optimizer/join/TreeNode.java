package com.chainmaker.jobservice.core.optimizer.join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-20 02:34
 * @description:用树结构记录要求交的表
 * @version: 1.0.0
 */

public class TreeNode<T> implements Iterable<TreeNode<T>> {

    public T data;

    public TreeNode<T> parent;

    public List<TreeNode<T>> children;

    private List<TreeNode<T>> elementsIndex;



    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<TreeNode<T>>();
        this.elementsIndex = new LinkedList<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    public boolean isRoot() {
        return parent == null;
    }


    public boolean isLeaf() {
        return children.size() == 0;
    }

    public void addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);

        childNode.parent = this;

        this.children.add(childNode);

        this.registerChildForSearch(childNode);

    }

    public int getLevel() {
        if (this.isRoot()) {
            return 0;
        } else {
            return parent.getLevel() + 1;
        }
    }

    private void registerChildForSearch(TreeNode<T> node) {
        elementsIndex.add(node);
        if (parent != null) {
            parent.registerChildForSearch(node);
        }
    }

    public TreeNode<T> findTreeNode(Comparable<T> cmp) {
        for (TreeNode<T> element : this.elementsIndex) {
            T elData = element.data;
            if (cmp.compareTo(elData) == 0)
                return element;
        }

        return null;
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        TreeNodeIterator<T> iterator = new TreeNodeIterator<T>(this);
        return iterator;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "[tree data null]";
    }


    public void insert(T par, T val) {
        if (this.data.equals(par)) {
            this.addChild(val);
        } else {
            for (int i=0; i<this.children.size(); i++) {
                this.children.get(i).insert(par, val);
            }
        }
    }

    private static String createIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public void printTree() {
        List<String> space = new ArrayList<>();
        String indent = createIndent(this.getLevel());
        System.out.println(indent + this.data);

        for (int i=0; i<this.children.size(); i++) {
            this.children.get(i).printTree();
        }
    }

}
