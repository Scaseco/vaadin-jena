package org.aksw.jenax.treequery2.impl;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.treequery2.api.RootedFacetTraversable;
import org.apache.jena.ext.com.google.common.base.Objects;

public abstract class RootedFacetTraversableBase<R, T extends RootedFacetTraversable<R, T>>
    implements RootedFacetTraversable<R, T>
{
    /** Roots are compared using reference equality! */
    protected R root;
    protected FacetPath path;

    public RootedFacetTraversableBase(R root, FacetPath path) {
        super();
        this.root = root;
        this.path = path;
    }

    @Override
    public R getRoot() {
        return root;
    }

//    @Override
//    public RootedFacetTraversableImpl<R> getOrCreateChild(FacetStep step) {
//        FacetPath newPath = path.resolve(step);
//        return new RootedFacetTraversableImpl<>(root, newPath);
//    }
//
//    protected T createChild(FacetStep step) {
//
//    }

    @Override
    public int hashCode() {
        return Objects.hashCode(root, path);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof RootedFacetTraversableBase) {
            RootedFacetTraversableBase<?, ?> o = (RootedFacetTraversableBase<?, ?>)obj;
            result = o.root == root && o.path.equals(path);
        }
        return result;
    }
}
