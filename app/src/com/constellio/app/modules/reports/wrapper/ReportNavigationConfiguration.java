package com.constellio.app.modules.reports.wrapper;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.management.labels.AddEditLabelViewImpl;
import com.constellio.app.ui.pages.management.labels.ListLabelViewImpl;
import com.constellio.model.entities.records.wrappers.User;

import java.io.Serializable;

/**
 * Created by Marco on 2017-01-24.
 */
public class ReportNavigationConfiguration implements Serializable {
    public static final String LABEL_MANAGEMENT = "labelManagement";
    public static final String ADD_LABEL_TEMPLATE = "addLabelTemplate";
    public static final String EDIT_LABEL_TEMPLATE = "editLabelTemplate";
    public static final String LABEL_MANAGEMENT_ICON = "icon";

    public static void configureNavigation(NavigationConfig config) {
        configureCollectionAdmin(config);
    }

    public static void configureNavigation(NavigatorConfigurationService service) {
        service.register(LABEL_MANAGEMENT, ListLabelViewImpl.class);
        service.register(ADD_LABEL_TEMPLATE, AddEditLabelViewImpl.class);
        service.register(EDIT_LABEL_TEMPLATE, AddEditLabelViewImpl.class);
    }


    private static void configureCollectionAdmin(NavigationConfig config) {
        config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(LABEL_MANAGEMENT, LABEL_MANAGEMENT_ICON) {
            @Override
            public void activate(Navigation navigate) {
                navigate.to().manageLabels();
            }

            @Override
            public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
                return ComponentState.ENABLED;
            }
        });
    }
}
