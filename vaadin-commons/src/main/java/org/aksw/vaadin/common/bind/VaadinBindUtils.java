package org.aksw.vaadin.common.bind;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableValue;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.shared.Registration;

public class VaadinBindUtils {

    public static <V> Registration bindSet(HasValue<?, Set<V>> hasValue, ObservableCollection<V> store) {
        // Collection<V> value = store.get();
        hasValue.setValue(new LinkedHashSet<>(store));

        Runnable deregister1 = store.addPropertyChangeListener(ev -> {
            Set<V> newValue = new LinkedHashSet<>((Collection<V>)ev.getNewValue());
            hasValue.setValue(newValue);
        });


        // Extra variable because of https://stackoverflow.com/questions/55532055/java-casting-java-11-throws-lambdaconversionexception-while-1-8-does-not
        ValueChangeListener<ValueChangeEvent<Set<V>>> listener = ev -> {

            // CollectionOps.sync();
            Collection<V> newValue = new LinkedHashSet<>(ev.getValue());
            store.replace(newValue);
//            store.clear();
//            store.addAll(newValue);
            // CollectionOps.smartDifference(store, newValue);
            // store.set(newValue);
            // newValue.clear();
            // newValue.addAll(newValue);
        };
        Registration deregister2 = hasValue.addValueChangeListener(listener);

        return () -> {
            deregister1.run();
            deregister2.remove();
        };
    }



    public static <V> Registration bind(HasValue<?, V> hasValue, ObservableValue<V> store) {
        V value = store.get();
        hasValue.setValue(value);

        Runnable deregister1 = store.addPropertyChangeListener(ev -> {
            V newValue = (V)ev.getNewValue();
            hasValue.setValue(newValue);
        });


        // Extra variable because of https://stackoverflow.com/questions/55532055/java-casting-java-11-throws-lambdaconversionexception-while-1-8-does-not
        ValueChangeListener<ValueChangeEvent<V>> listener = ev -> {
            V newValue = ev.getValue();
            store.set(newValue);
        };
        Registration deregister2 = hasValue.addValueChangeListener(listener);

        return () -> {
            deregister1.run();
            deregister2.remove();
        };
    }
}
