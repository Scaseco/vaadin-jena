package org.aksw.vaadin.app.demo;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgrImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.reactivex.rxjava3.core.Flowable;

@Configuration
public class ConfigComponentDemoVaadin {
    @Bean
    public LabelServiceSwitchable<Node, String> labelService() {
        RdfDataSource base = () -> RDFConnection.connect("http://localhost:8642/sparql");

        RdfDataSource dataSource = base
                .decorate(RdfDataSourceWithBnodeRewrite::wrapWithAutoBnodeProfileDetection)
                // .decorate(RdfDataSourceWithLocalCache::new)
                ;

        QueryExecutionFactoryQuery qef = QueryExecutionFactories.of(dataSource); // QueryExecutionFactories.of(dataSource);
        Property labelProperty = RDFS.label;// DCTerms.description;

        LookupService<Node, String> ls1 = LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get(), 30);
        LookupService<Node, String> ls2 = keys -> Flowable.fromIterable(keys).map(k -> Map.entry(k, Objects.toString(k)));


        VaadinRdfLabelMgrImpl labelMgr = new VaadinRdfLabelMgrImpl(ls1);

        LabelServiceSwitchable<Node, String> result = new LabelServiceSwitchable<>(labelMgr);
        result.getLookupServices().addAll(Arrays.asList(ls1, ls2));

        return result;
    }

}
