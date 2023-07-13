package org.aksw.jenax.treequery2.impl;

import java.nio.charset.StandardCharsets;

import org.aksw.facete.v4.impl.FacetPathUtils;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.treequery2.api.FacetPathMapping;
import org.aksw.jenax.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.treequery2.api.ScopedVar;
import org.aksw.jenax.treequery2.api.VarScope;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * Mapping of facet paths to hashes. In case of hash clashes a sequence number
 * is appended to the hash codes.
 */
public class FacetPathMappingImpl
    implements FacetPathMapping
{
    private static final Logger logger = LoggerFactory.getLogger(FacetPathMappingImpl.class);

    /** So far allocated mappings */
    protected BiMap<FacetPath, String> pathToName = HashBiMap.create();

    protected HashFunction hashing;
    protected BaseEncoding encoding;

    /**
     * Default construction using Guava's Hashing.murmur3_32_fixed()
     * and BaseEncoding.base32().omitPadding().
     */
    public FacetPathMappingImpl() {
        this(Hashing.murmur3_32_fixed(), BaseEncoding.base32().omitPadding());
    }

    public FacetPathMappingImpl(HashFunction hashing, BaseEncoding encoding) {
        super();
        this.hashing = hashing;
        this.encoding = encoding;
    }

    public BiMap<FacetPath, String> getPathToName() {
        return pathToName;
    }

    // XXX We could always allocate names for all intermediate paths
//    public String allocate(FacetPath path, FacetStep step) {
//
//    }

    @Override
    public String allocate(FacetPath rawFacetPath) {
        FacetPath facetPath = FacetPathUtils.toElementId(rawFacetPath);
        String result = pathToName.computeIfAbsent(facetPath, fp -> {
            byte[] bytes = hashing.hashString(facetPath.toString(), StandardCharsets.UTF_8).asBytes();
            String r = encoding.encode(bytes).toLowerCase();
            BiMap<String, FacetPath> nameToPath = pathToName.inverse();
            for (int i = 0; ; ++i) {
                String test = i == 0 ? r : r + i;
                FacetPath clashPath = nameToPath.get(test);
                if (clashPath == null) {
                    r = test;
                    break;
                } else {
                    // log level debug?
                    logger.info("Mitigated hash clash: Hash " + test + " clashed for [" + fp + "] and [" + clashPath + "]");
                }
            }
            return r;
        });
        return result;
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, ScopedFacetPath sfp) {
        return resolveVar(facetPathMapping, sfp.getScope(), sfp.getFacetPath());
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, VarScope scope, FacetPath facetPath) {
        return resolveVar(facetPathMapping, scope.getScopeName(), scope.getStartVar(), facetPath);
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, String baseScopeName, Var rootVar, FacetPath facetPath) {
        ScopedVar result;
        // Empty path always resolves to the root var
        if (facetPath.getParent() == null) {
            result = ScopedVar.of("", "", rootVar);
        } else {
            FacetPath parentPath = facetPath.getParent();
            FacetStep lastStep = facetPath.getFileName().toSegment();
            Node component = lastStep.getTargetComponent();
            // FacetPath eltId = FacetPathUtils.toElementId(facetPath);
            if (FacetStep.isSource(component)) {
                result = resolveVar(facetPathMapping, baseScopeName, rootVar, parentPath);
            } else {
                String pathScopeName = facetPathMapping.allocate(facetPath);
                result = ScopedVar.of(baseScopeName, pathScopeName, (Var)component);
            }
        }
        return result;
    }
}
