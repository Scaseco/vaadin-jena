package org.aksw.vaadin.common.provider.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class TaskManager {
    public static void setupActionGrid(MenuBar menuBar, TaskControlRegistryImpl taskControlRegistry) {
        Span actionMenuArea = new Span();
        Button progressBarBtn = new Button(VaadinIcon.PROGRESSBAR.create());

        Span taskCountPendingSpan = new Span();
        taskCountPendingSpan.getElement().getThemeList().add("badge pill");

        Span taskCountSuccessSpan = new Span();
        taskCountSuccessSpan.getElement().getThemeList().add("badge success pill");

        Span taskCountErrorSpan = new Span();
        taskCountErrorSpan.getElement().getThemeList().add("badge error pill");

        actionMenuArea.add(progressBarBtn, taskCountPendingSpan, taskCountSuccessSpan, taskCountErrorSpan);

        MenuItem actionMenu = menuBar.addItem(actionMenuArea);

        SubMenu actionSubMenu = actionMenu.getSubMenu();


        HierarchicalDataProvider<TaskControl<?>, ?> actionTdp = taskControlRegistry.getTreeDataProvider();

        Grid<TaskControl<?>> actionGrid = new TreeGrid<>();
        actionGrid.setWidth("300px");
        actionGrid.setAllRowsVisible(true);

        actionGrid.addComponentColumn(o -> {

            Div progressBarLabel = new Div();
            progressBarLabel.setText("Task [" + o.getLabel() + "]");

            ProgressBar progressBar = new ProgressBar();

            Div progressBarWrapper = new Div(progressBarLabel, progressBar);
            progressBarWrapper.setWidthFull();

            HorizontalLayout r = new HorizontalLayout();
            r.add(progressBarWrapper);
            r.setFlexGrow(1, progressBarWrapper);

            if (o.isComplete()) {
                Throwable throwable = o.getThrowable();
                boolean isSuccess = throwable == null;

                progressBar.setMin(0f);
                progressBar.setMax(1f);
                progressBar.setValue(1f);
                if (isSuccess) {
                    progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
                    Icon icon = VaadinIcon.CHECK_CIRCLE_O.create();
                    icon.getElement().getThemeList().add("badge success pill");
                    r.add(icon);
                } else {
                    progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                    Icon icon = VaadinIcon.CLOSE_CIRCLE_O.create();
                    icon.getElement().getThemeList().add("badge error pill");
                    r.add(icon);
                }
            } else {
                progressBar.setIndeterminate(true);
                Icon icon = VaadinIcon.STOP.create();
                icon.getElement().getThemeList().add("badge error pill");
                Button cancelBtn = new Button(icon);
                r.add(cancelBtn);
                cancelBtn.addClickListener(ev -> {
                    o.abort();
                });
            }

            return r;
        }).setKey("value");

        actionGrid.setDataProvider(actionTdp);
        actionTdp.addDataProviderListener(ev -> {
            // pending success error
            long[] pse = {0, 0, 0};

            try (Stream<TaskControl<?>> stream = actionTdp.fetchChildren(new HierarchicalQuery<>(null, null))) {
                List<TaskControl<?>> tasks = stream.collect(Collectors.toList());
                for (TaskControl<?> task: tasks) {
                    int classify = !task.isComplete() ? 0 : task.getThrowable() == null ? 1 : 2;
                    ++pse[classify];
                }
            }

            taskCountPendingSpan.setVisible(pse[0] != 0);
            taskCountPendingSpan.setText(Long.toString(pse[0]));

            taskCountSuccessSpan.setVisible(pse[1] != 0);
            taskCountSuccessSpan.setText(Long.toString(pse[1]));

            taskCountErrorSpan.setVisible(pse[2] != 0);
            taskCountErrorSpan.setText(Long.toString(pse[2]));
        });

        actionSubMenu.addComponent(actionGrid);
    }
}
