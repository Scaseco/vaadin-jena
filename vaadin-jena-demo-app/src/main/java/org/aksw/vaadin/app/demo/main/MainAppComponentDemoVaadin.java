package org.aksw.vaadin.app.demo.main;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MainAppComponentDemoVaadin
    extends SpringBootServletInitializer
{
    public static void main(String[] args) {

        ConfigurableApplicationContext cxt = new SpringApplicationBuilder()
                .bannerMode(Mode.OFF)
                .sources(MainAppComponentDemoVaadin.class)
                .run(args);
    }
}
