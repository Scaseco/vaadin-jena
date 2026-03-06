package org.aksw.vaadin.app.demo;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@PWA(name = "Semantic Components Vaadin Demonstrator", shortName = "Scaseco Demo",
    description = "This is a demonstrator of components for semantic data.")
// @StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.COMPACT_STYLESHEET)
@Push
public class MainAppComponentDemoVaadin
    implements AppShellConfigurator
{
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        SpringApplication.run(MainAppComponentDemoVaadin.class, args);
//        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
//                .bannerMode(Mode.OFF)
//                .sources(MainAppComponentDemoVaadin.class)
//                .run(args);
    }
}
