package org.aksw.vaadin.app.demo;

import org.aksw.vaadin.app.demo.view.edit.resource.ResourceEditorView;
import org.aksw.vaadin.app.demo.view.welcome.LandingPageView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

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


    public MainLayout () {
        drawerToggle = new DrawerToggle();

        H1 title = new H1("Vaadin / Jena Demo");
        title.getStyle()
          .set("font-size", "var(--lumo-font-size-l)")
          .set("margin", "0");

        // setDrawerOpened(true);
        addToNavbar(drawerToggle, title);
        addToDrawer(getTabs());
    }


    private Tabs getTabs() {
        RouteTabs tabs = new RouteTabs();
        tabs.add(
                createTab(VaadinIcon.HOME, "Home", LandingPageView.class),
                createTab(VaadinIcon.FOLDER_ADD, "New Data Project", ResourceEditorView.class)
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

    private RouterLink createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> routeClass) {
      Icon icon = viewIcon.create();
      icon.getStyle()
              .set("box-sizing", "border-box")
              .set("margin-inline-end", "var(--lumo-space-m)")
              .set("margin-inline-start", "var(--lumo-space-xs)")
              .set("padding", "var(--lumo-space-xs)");

      RouterLink link = new RouterLink();
      link.add(icon, new Span(viewName));
      link.setTabIndex(-1);
      link.setRoute(routeClass);

      // return new Tab(link);
      return link;
    }

}