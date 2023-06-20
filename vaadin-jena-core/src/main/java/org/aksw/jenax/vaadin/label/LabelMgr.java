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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.rx.lookup.LookupService;
import org.locationtech.jts.awt.PointShapeFactory.X;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;


/**
 * <b>IMPORTANT</b>: Callbacks should only operate on the the component passed in as
 * the argument and must not keep a reference to that component. Otherwise the weak-reference
 * clean up mechanism will not trigger.
 *
 * @author raven
 *
 * @param <C>
 * @param <R>
 * @param <L>
 */
public class LabelMgr<R, L>
    implements LabelService<R, L>
{

    private static final Object DUMMY = new Object();

    private static final Logger logger = LoggerFactory.getLogger(LabelMgr.class);

    public static class State<R, L> {
        protected Set<R> resources = new LinkedHashSet<>();
        protected BiConsumer<Object, Map<R, L>> labelCallback;
    }

    /** A cache with weak keys pointing to resources */
    protected LoadingCache<Object, State<R, L>> componentToResources;

    protected Map<R, L> activeLabels = new HashMap<>();
    protected Cache<R, L> labelCache;

    protected Map<R, Long> resourceRefCount = new ConcurrentHashMap<>();

    // The set of components that are awaiting updates
    protected Map<Object, Object> pendingComponents = CacheBuilder.newBuilder().weakKeys().<Object, Object>build().asMap();
    // Collections.synchronizedMap(new IdentityHashMap<>()); // CacheBuilder.newBuilder().weakKeys().<Object, Object>build().asMap();
//     new MapMaker().weakKeys(). //Collections.synchronizedMap(new WeakHashMap<>());

    // The set of resources for which to request labels on the next iteration
    protected Set<R> pendingResources = new HashSet<>();

    protected LookupService<R, L> lookupService;

    public LabelMgr(LookupService<R, L> lookupService) {
        super();
        this.lookupService = lookupService;
        this.labelCache = CacheBuilder
                .newBuilder()
                .maximumSize(10000)
                .build();

      componentToResources = CacheBuilder.newBuilder()
          .removalListener((RemovalNotification<Object, State<R, L>> n) -> {
              synchronized (n) {
                  Set<R> resources = n.getValue().resources;
                  for (R r : resources) {
                      decRefCount(r);
                  }
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

    public void setLookupService(LookupService<R, L> lookupService) {
        this.lookupService = lookupService;
    }

    public LookupService<R, L> getLookupService() {
        return lookupService;
    }

    public void refreshAll() {
        for (Entry<Object, State<R, L>> e : componentToResources.asMap().entrySet()) {
            pendingComponents.put(e.getKey(), DUMMY);
            pendingResources.addAll(e.getValue().resources);
        }
        activeLabels.clear();
        labelCache.invalidateAll();
        scheduleRetrieval();
    }


    protected synchronized void loadPending() {
        List<Object> components = new ArrayList<>(pendingComponents.keySet());
        Set<R> resources = new HashSet<>(pendingResources);

        logger.debug(String.format("Retrieving labels for %d resources and %d components", resources.size(), components.size()));
        pendingResources = new HashSet<>();

        Map<R, L> map = lookupService.fetchMap(resources);
        for (Entry<R, L> e : map.entrySet()) {
            R resource = e.getKey();
            L label = e.getValue();

            // Here we handle a corner case: A label was requested but once it became
            // asynchronously available the client had already lost interest in it
            // - in that case add it directly to the cache rather than the active set.
            resourceRefCount.compute(resource, (k, v) -> {
                if (v != null) {
                    activeLabels.put(resource, label);
                } else {
                    labelCache.put(resource, label);
                }
                return v;
            });
        }

        scheduleApplication(components);
    }

    public void scheduleRetrieval() {
        loadPending();
    }

    protected void scheduleApplication(Collection<Object> components) {
        for (Object c : components) {
            applyNow(c);
        }
    }

    /**
     * Attempt to immediately apply all labels to a component's resources.
     * Returns false if any resource is not in the active set.
     */
    protected boolean applyNow(Object component) {
        State<R, L> state = componentToResources.getIfPresent(component);

        boolean result = false;
        if (state != null) {
            synchronized (state) {
                // Check for whether all labels are available - if so, apply them
                result = state.resources.stream().allMatch(activeLabels::containsKey);

                if (result) {
                    state.labelCallback.accept(component, activeLabels);
                }
            }
        }

        return result;
    }

    public void invalidateAll() {
        List<Object> components = new ArrayList<>(componentToResources.asMap().keySet());
        scheduleApplication(components);
    }


    protected void incRefCount(R r) {
        resourceRefCount.compute(r, (k, v) -> {
            long x = v == null ? 1l : v + 1l;

            if (x == 1) {
                // if the new ref count is 1 then the label cannot be active - trigger loading
                L label = labelCache.getIfPresent(r);
                if (label != null) {
                    activeLabels.put(r, label);
                } else {
                    pendingResources.add(r);
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
            long tmp = v - 1;
            Long  x = tmp == 0 ? null : tmp;

            if (x == null) {
                // Delete an existing active label entry and transfer it to
                // the labelCache
                activeLabels.compute(k, (kk, label) -> {
                    if (label != null) {
                        labelCache.put(k, label);
                    }
                    return null;
                });
//
//                L label = activeLabels.get(k);
//                activeLabels.remove(k);
//                if (label != null) {
//                    labelCache.put(k, label);
//                }

                pendingResources.remove(r);
            }

            return x;
        });

    }

//    public void fetchLabels() {
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
//    }

//    LabelService.register(Node node, Callable<?> callback) {
//
//    }


    /** somePath, pathDecomposer, pathLabelComposer
     * do we need a context for the label?
     * function<Resource, Context, label>
     */
    public static <C, T, R, L> void register(
            C component, T obj,
            Function<? super T, Set<X>> decomposer,
            BiFunction<T, Map<R, L>, L> composer) {
    }


    /** Convenience method for a single resource. */
    public <X> void register(X component, R resource, BiConsumer<? super X, Map<R, L>> callback) {
        register(component, Collections.singleton(resource), callback);
    }

    /**
     * <b>Registrations are subject to GC when the component gets GC'd.
     * For this to work, callbacks MUST NOT strongly reference the component. Instead,
     * the component is passed in as the first argument.</b>
     *
     * <p>
     *
     * Register a set of resources for which to fetch labels with a component.
     * If there is already a prior registration for that component then this
     * new request overrides it. This means any prior callback will no
     * longer be invoked.
     *
     * If all labels are found in the cache then the callback is invoked
     * immediately and no waiting for another UI referesh cycle is needed.
     *
     * @param <X>
     * @param component
     * @param resources
     * @param callback
     */
    @SuppressWarnings("unchecked")
    public <X> void register(X component, Set<R> resources, BiConsumer<? super X, Map<R, L>> callback) {
        State<R, L> entry;
        try {
            entry = componentToResources.get(component);
            // System.out.println("Lookup for component " + ((HasText)component).getText() + " with resources " + resources);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        synchronized (entry) { // synchronize on component?

            Set<R> prior = entry.resources;
            Set<R> removals = Sets.difference(prior, resources);
            Set<R> additions = Sets.difference(resources, prior);

            // System.out.println("Label removals: " + removals);
            // System.out.println("Label additions: " + additions);

            entry.resources = resources;
            entry.labelCallback = (o, lm) -> callback.accept((X)o, lm);

            additions.forEach(this::incRefCount);
            removals.forEach(this::decRefCount);

            if (!additions.isEmpty()) {
                boolean couldApplyImmediately = applyNow(component);
                if (!couldApplyImmediately) {
                    pendingComponents.put(component, DUMMY);

                    scheduleRetrieval();
                }
            }
        }
    }
}
