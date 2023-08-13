package org.aksw.vaadin.app.demo;

import java.util.Map;
import java.util.Objects;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactories;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.LabelService;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgrImpl;
import org.aksw.vaadin.app.demo.view.edit.resource.ResourceEditorView;
import org.aksw.vaadin.app.demo.view.label.LabelView;
import org.aksw.vaadin.app.demo.view.shaclgrid.ShaclGridView;
import org.aksw.vaadin.app.demo.view.tablemapper.TableMapperView;
import org.aksw.vaadin.app.demo.view.welcome.LandingPageView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import io.reactivex.rxjava3.core.Flowable;

@Route("")
@PWA(name = "Semantic Components Vaadin Demonstrator", shortName = "Scaseco Demo",
        description = "This is a demonstrator of components for semantic data.", enableInstallPrompt = true)
@CssImport(value = "./styles/shared-styles.css", include = "lumo-badge")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
//@CssImport(value = "./styles/vaadin-tab-styles.css", themeFor = "vaadin-tab")
@CssImport(value = "./styles/vaadin-select-text-field-styles.css", themeFor = "vaadin-select-text-field")
@CssImport(value = "./styles/vaadin-select-styles.css", themeFor = "vaadin-select")
@CssImport(value = "./styles/vaadin-text-area-styles.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/flow-component-renderer-styles.css", themeFor = "flow-component-renderer")
@CssImport(value = "./styles/vaadin-grid-tree-toggle-styles.css", themeFor = "vaadin-grid-tree-toggle")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@Theme(value = Lumo.class)
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
//@HtmlImport(value="frontend://bower_components/vaadin-lumo-styles/badge.html")
public class MainLayout
    extends AppLayout
{
    protected DrawerToggle drawerToggle;
    protected Button mainViewBtn;
    protected Button newDataProjectBtn;
    protected Button connectionMgmtBtn;

    @Autowired
    protected LabelServiceSwitchable<Node, String> labelService;

    public MainLayout () {
        drawerToggle = new DrawerToggle();

        H1 title = new H1("Vaadin / Jena Demo");
        title.getStyle()
          .set("font-size", "var(--lumo-font-size-l)")
          .set("margin", "0");

        addToNavbar(drawerToggle, title);

        setupNavbar();

        addToDrawer(getTabs());
    }


    private void setupNavbar() {
        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button themeToggleButton = new Button(new Icon(VaadinIcon.LIGHTBULB), click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
              themeList.remove(Lumo.DARK);
            } else {
              themeList.add(Lumo.DARK);
            }
        });
        navbarLayout.add(themeToggleButton);

        Button resetLabelsBtn = new Button(new Icon(VaadinIcon.TEXT_LABEL), ev -> {
            //LookupService<Node, String> ls = labelMgr.getLookupService() == ls1 ? ls2 : ls1;
            labelService.next();
            labelService.refreshAll();
        });
        navbarLayout.add(resetLabelsBtn);

        addToNavbar(navbarLayout);

    }

    private Tabs getTabs() {

        RouteTabs tabs = new RouteTabs();
        tabs.add(
                RouteTabs.newTab(VaadinIcon.HOME, "Home", LandingPageView.class),
                RouteTabs.newTab(VaadinIcon.FOLDER_ADD, "New Data Project", ResourceEditorView.class),
                RouteTabs.newTab(VaadinIcon.EYE, "Labels", LabelView.class),
                RouteTabs.newTab(VaadinIcon.TABLE, "TableMapper", TableMapperView.class),
                RouteTabs.newTab(VaadinIcon.LINK, "ShaclGrid", ShaclGridView.class)
//                createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
//                createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class),
//                createTab(VaadinIcon.DATABASE, "Catalogs", CatalogMgmtView.class)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
    //  Tabs tabs = new Tabs();
    //  tabs.add(
    //    createTab(VaadinIcon.HOME, "Home", DmanLandingPageView.class),
    //    createTab(VaadinIcon.FOLDER_ADD, "New Data Project", NewDataProjectView.class),
    //    createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
    //    createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class)
    //  );
    //  tabs.setOrientation(Tabs.Orientation.VERTICAL);
      return tabs;
    }
}
