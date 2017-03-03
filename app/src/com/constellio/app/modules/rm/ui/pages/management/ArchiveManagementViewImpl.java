package com.constellio.app.modules.rm.ui.pages.management;

import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class ArchiveManagementViewImpl extends BaseViewImpl implements ArchiveManagementView {
    public static final ThemeResource DECOMMISSIONING_ICON = new ThemeResource("images/icons/config/platform_truck.png");
    public static final String DECOMMISSIONING = "decommissioning-caption";
    public static final ThemeResource NEW_CONTAINER_ICON = new ThemeResource("images/icons/config/container-add.png");
    public static final String NEW_CONTAINER = "new-container";
    public static final ThemeResource MULTIPLE_CONTAINERS_ICON = new ThemeResource("images/icons/config/boxes-add.png");
    public static final String MULTIPLE_CONTAINERS = "new-boxes";
    public static final ThemeResource CONTAINERS_ICON = new ThemeResource("images/icons/config/box.png");
    public static final String CONTAINERS = "containers-caption";
    public static final ThemeResource REPORTS_ICON = new ThemeResource("images/icons/config/report.png");
    public static final String REPORTS = "reports-caption";
    public static final String AVAILABLE_SPACE = "availableSpace-caption";
    public static final ThemeResource AVAILABLE_SPACE_ICON = new ThemeResource("images/icons/config/shelf.png");

    private final ArchiveManagementPresenter presenter;

    private Button decommissioning, multipleContainers, newContainer, containers, reports, availableSpace;

    public ArchiveManagementViewImpl() {
        presenter = new ArchiveManagementPresenter(this);
    }

    @Override
    protected String getTitle() {
        return $("ArchiveManagementView.viewTitle");
    }

    @Override
    protected Component buildMainComponent(ViewChangeEvent event) {
        decommissioning = new IconButton(DECOMMISSIONING_ICON, $("ArchiveManagementView.decommissioning"), false) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.decommissioningButtonClicked();
            }
        };
        decommissioning.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        decommissioning.addStyleName(DECOMMISSIONING);

        newContainer = new IconButton(NEW_CONTAINER_ICON, $("ArchiveManagementView.newContainer"), false) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.newContainerButtonClicked();
            }
        };
        newContainer.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        newContainer.addStyleName(NEW_CONTAINER);

        multipleContainers = new IconButton(MULTIPLE_CONTAINERS_ICON, $("ArchiveManagementView.newContainers"), false) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.multipleContainersButtonClicked();
            }
        };
        multipleContainers.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        multipleContainers.addStyleName(MULTIPLE_CONTAINERS);
        multipleContainers.setVisible(presenter.isMultipleContainersButtonVisible());

        containers = new IconButton(CONTAINERS_ICON, $("ArchiveManagementView.containers"), false) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.containersButtonClicked();
            }
        };
        containers.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        containers.addStyleName(CONTAINERS);

        reports = new IconButton(REPORTS_ICON, $("ArchiveManagementView.reports"), false) {
            @Override
            protected void buttonClick(ClickEvent event) {
                presenter.reportsButtonClicked();
            }
        };
        reports.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        reports.addStyleName(REPORTS);

//        availableSpace = new IconButton(AVAILABLE_SPACE_ICON, $("ArchiveManagementView.availableSpace"), false) {
//            @Override
//            protected void buttonClick(ClickEvent event) {
//
//            }
//        };
//        availableSpace.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
//        availableSpace.addStyleName(AVAILABLE_SPACE);

        presenter.onViewAssembled();
        CssLayout layout = new CssLayout(decommissioning, multipleContainers, newContainer, containers, reports);
        layout.addStyleName("view-group");

        return layout;
    }

    @Override
    public void setDecommissioningButtonVisible(boolean visible) {
        decommissioning.setVisible(visible);
    }

    @Override
    public void setNewContainerButtonVisible(boolean visible) {
        newContainer.setVisible(visible);
    }

    @Override
    public void setContainersButtonVisible(boolean visible) {
        containers.setVisible(visible);
    }

    @Override
    public void setPrintReportsButtonVisible(boolean visible) {
        reports.setVisible(visible);
    }
}
