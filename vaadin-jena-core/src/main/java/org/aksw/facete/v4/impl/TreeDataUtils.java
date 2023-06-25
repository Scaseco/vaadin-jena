package org.aksw.facete.v4.impl;

import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.vaadin.flow.data.provider.hierarchy.TreeData;

/** Utilities for Vaadin's TreeData class */
public class TreeDataUtils {
    /** Add an item to a TreeData structure by recursively adding its ancestors first. */
    public static <T> void putItem(TreeData<T> treeData, T item, Function<? super T, ? extends T> getParent) {
        Preconditions.checkNotNull(item);
        if (!treeData.contains(item)) {
            T parent = getParent.apply(item);
            if (parent != null) {
                putItem(treeData, parent, getParent);
            }
            treeData.addItem(parent, item);
        }
    }

    /** Bridge between Vaadin's and our TreeData structure */
    public static <T> TreeData<T> copyToVaadin(TreeData<T> target, org.aksw.facete.v3.api.TreeData<T> source) {
        target.addItems(source.getRootItems(), source::getChildren);
        return target;
    }

    public static <T> TreeData<T> toVaadin(org.aksw.facete.v3.api.TreeData<T> source) {
        return copyToVaadin(new TreeData<>(), source);
    }

    public static <T> org.aksw.facete.v3.api.TreeData<T> copyToFacete(org.aksw.facete.v3.api.TreeData<T> target, TreeData<T> source) {
        target.addItems(source.getRootItems(), source::getChildren);
        return target;
    }

    public static <T> org.aksw.facete.v3.api.TreeData<T> toFacete(TreeData<T> source) {
        return copyToFacete(new org.aksw.facete.v3.api.TreeData<>(), source);
    }
}
