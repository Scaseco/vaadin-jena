package org.aksw.vaadin.app.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@SpringBootApplication
@PWA(name = "Semantic Components Vaadin Demonstrator", shortName = "Scaseco Demo",
    description = "This is a demonstrator of components for semantic data.")
@Theme(themeClass = Lumo.class)
@Push
public class MainAppComponentDemoVaadin
    implements AppShellConfigurator
{
    public static void main(String[] args) {
        SpringApplication.run(MainAppComponentDemoVaadin.class, args);
//        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
//                .bannerMode(Mode.OFF)
//                .sources(MainAppComponentDemoVaadin.class)
//                .run(args);
    }
}
