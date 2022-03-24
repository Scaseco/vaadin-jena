package org.aksw.jenax.vaadin.label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import org.aksw.commons.rx.lookup.LookupService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;


/**
 * <b>IMPORTANT</b>: Callbacks should only operate on the the component passed in as
 * the argument and must not keep a reference to that component. Otherwise the weak-reference
 * clean up mechanism will not trigger.s
 *
 * @author raven
 *
 * @param <C>
 * @param <R>
 * @param <L>
 */
public class LabelService<C, R, L> {

    public static class State<R, L> {
        protected Set<R> resources = new LinkedHashSet<>();
        protected BiConsumer<Object, Map<R, L>> labelCallback;
    }

    /** A cache with weak keys pointing to resources*/
    protected LoadingCache<Object, State<R, L>> componentToResources;

    protected Map<R, L> activeLabels = new HashMap<>();
    protected Cache<R, L> labelCache;

    protected Map<R, Long> resourceRefCount = new ConcurrentHashMap<>();
    // protected loadingCache;

    // The set of components that are awaiting updates
    protected Map<Object, Void> pendingComponents = Collections.synchronizedMap(new WeakHashMap<>());

    // The set of resources for which to request labels on the next iteration
    protected Set<R> pending = new HashSet<>();


    protected LookupService<R, L> lookupService;

    public LabelService(LookupService<R, L> lookupService) {
        super();
        this.lookupService = lookupService;
        this.labelCache = CacheBuilder
                .newBuilder()
                .maximumSize(10000)
                .build();

      componentToResources = CacheBuilder.newBuilder()
          .removalListener((RemovalNotification<Object, State<R, L>> n) -> {
              Set<R> resources = n.getValue().resources;
              for (R r : resources) {
                  decRefCount(r);
              }

              // resourceRefCount
          })
          .weakKeys()
          .build(new CacheLoader<Object, State<R, L>>() {
              @Override
              public State<R, L> load(Object key) throws Exception {
                  return new State<R, L>();
              }
          });


    }

    protected void loadPending() {
        List<Object> components = new ArrayList<>(pendingComponents.keySet());
        Set<R> resources = new HashSet<>(pending);
        pending = new HashSet<>();

        Map<R, L> map = lookupService.fetchMap(resources);
        for (Entry<R, L> e : map.entrySet()) {
            R resource = e.getKey();
            L label = e.getValue();

            if (resourceRefCount.containsKey(resource)) {
                activeLabels.put(resource, label);
            } else {
                labelCache.put(resource, label);
            }
        }

        scheduleApplication(components);
    }

    public void scheduleRetrieval() {
        loadPending();
    }

    protected void scheduleApplication(Collection<Object> components) {
        for (Object c : components) {
            State<R, L> state = componentToResources.getIfPresent(c);

            if (state != null) {
                state.labelCallback.accept(c, activeLabels);
//                for (R resource : state.resources) {
//                    // L label = activeLabels.get(resource);
//                }
            }
        }
    }

    protected void incRefCount(R r) {
        resourceRefCount.compute(r, (k, v) -> {
            long x = v == null ? 1l : v + 1l;

            if (x == 1) {
                // trigger loading
                L label = labelCache.getIfPresent(r);
                if (label != null) {
                    activeLabels.put(r, label);
                } else {
                    pending.add(r);
                }
            }

            return x;
        });
    }


    protected void decRefCount(R r) {
        resourceRefCount.compute(r, (k, v) -> {
            if (v == null) {
                throw new IllegalStateException("ref count went below zero - should not happen");
            }
            long tmp = v -1;
            Long  x = tmp == 0 ? null : tmp;

            if (x == null) {
                activeLabels.remove(k);
                pending.remove(r);
            }

            return x;
        });

    }

    public void fetchLabels() {
//        componentToResources = CacheBuilder.newBuilder()
//            .removalListener((RemovalNotification<C, State<R, L>> n) -> {
//                Set<R> resources = n.getValue();
//
//                // resourceRefCount
//            })
//            .weakKeys()
//            .build(new CacheLoader<C, State<R, L>>() {
//                @Override
//                public State<R, L> load(C key) throws Exception {
//                    return new State<R, L>();
//                }
//            });

//
//        UI.getCurrent().access(() -> {
//            map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
//            FeatureGroup group = new FeatureGroup();
//            group.addTo(map);
//            GeoJsonJenaUtils.toWgs84GeoJson(gw).addTo(group);
//            map.fitBounds(Leaflet4VaadinJenaUtils.getWgs84Envelope(gw));
//        });
    }

//    LabelService.register(Node node, Callable<?> callback) {
//
//    }


    public <X> void register(X component, Set<R> resources, BiConsumer<? super X, Map<R, L>> callback) {
        State<R, L> entry;
        try {
            entry = componentToResources.get(component);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        Set<R> prior = entry.resources;
        Set<R> removals = Sets.difference(prior, resources);
        Set<R> additions = Sets.difference(resources, prior);

        entry.resources = resources;
        entry.labelCallback = (o, lm) -> callback.accept((X)o, lm);

        additions.forEach(this::incRefCount);
        removals.forEach(this::decRefCount);

        if (!additions.isEmpty()) {
            pendingComponents.put(component, null);

            scheduleRetrieval();
        }
    }
}
