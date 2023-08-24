package org.aksw.jena_sparql_api.vaadin.data.provider;

//public class DataProviderTree
//    extends AbstractBackEndDataProvider<Binding, Expr> {
//
//    protected RdfDataSource dataSource;
//    protected UnaryRelation baseConcept;
//    protected TreeData<FacetPath> facetTree;
//    protected Map<FacetPath, Boolean> pathToVisibility;
//
//    public DataProviderTree(
//            RdfDataSource dataSource,
//            UnaryRelation baseConcept,
//            TreeData<FacetPath> treeData,
//            Map<FacetPath, Boolean> pathToVisibility
//    ) {
//        this.dataSource = dataSource;
//        this.baseConcept = baseConcept;
//        this.facetTree = treeData;
//        this.pathToVisibility = pathToVisibility;
//    }
//
//    @Override
//    protected Stream<Binding> fetchFromBackEnd(Query<Binding, Expr> query) {
//        SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();
//
//        // org.aksw.facete.v3.api.TreeData<FacetPath> treeProjection = TreeDataUtils.toFacete(treeDataProvider.getTreeData());
//        // TreeDataUtils.toFacete(facetTree);
//
//        MappedQuery mappedQuery = ElementGenerator.createQuery(baseConcept, facetTree, constraintIndex, path -> !Boolean.FALSE.equals(pathToVisibility.get(path)));
//
//        mappedQuery = new MappedQuery(mappedQuery.getTree(), QueryGenerationUtils.discardUnbound(mappedQuery.getQuery()), mappedQuery.getVarToPath());
//
//    }
//
//    @Override
//    protected int sizeInBackEnd(Query<Binding, Expr> query) {
//        // TODO Auto-generated method stub
//        return 0;
//    }
//}
