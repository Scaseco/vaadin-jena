package org.aksw.vaadin.app.demo.view.edit.resource;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorDirect;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.jenax.path.core.PathOpsPP;
import org.aksw.jenax.path.core.PathPP;

public class PathPPModelImpl
    extends ObservableValueImpl<PathPP>
    implements PathPPModel
{
    public PathPPModelImpl(SingleValuedAccessor<PathPP> delegate) {
        super(delegate);
    }

    public static PathPPModel create() {
        return new PathPPModelImpl(new SingleValuedAccessorDirect<>(PathOpsPP.get().newRoot()));
    }
}
