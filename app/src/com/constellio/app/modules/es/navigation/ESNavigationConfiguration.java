package com.constellio.app.modules.es.navigation;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.modules.es.ui.pages.*;
import com.constellio.app.modules.es.ui.pages.mapping.AddEditMappingViewImpl;
import com.constellio.app.modules.es.ui.pages.mapping.DisplayConnectorMappingsViewImpl;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.app.ui.pages.home.TaxonomyTabSheet;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ESNavigationConfiguration implements Serializable {
	public static final String CONNECTORS = "connectors";
	public static final String CONNECTORS_ICON = "images/icons/config/connector.png";
	public static final String SEARCH_ENGINE = "searchEngine";
	public static final String SEARCH_ENGINE_ICON = "images/icons/config/configuration-search.png";

	public static final String TAXONOMIES = "taxonomies";
    public static final String CONNECTOR_REPORT = "connectorReport";
    public static final String DISPLAY_CONNECTOR_INSTANCE = "displayConnectorInstance";
    public static final String ADD_CONNECTOR_MAPPING = "addConnectorMapping";
    public static final String EDIT_CONNECTOR_MAPPING = "editConnectorMapping";
    public static final String DISPLAY_CONNECTOR_MAPPINGS = "displayConnectorMappings";
    public static final String EDIT_CONNECTOR_INSTANCE = "editConnectorInstance";
    public static final String LIST_CONNECTOR_INSTANCES = "connectorInstances";
    public static final String WIZARD_CONNECTOR_INSTANCE = "wizardConnectorInstance";

    public static void configureNavigation(NavigationConfig config) {
		configureCollectionAdmin(config);
		configureHomeFragments(config);
	}

	public static void configureNavigation(NavigatorConfigurationService service) {
		service.register(CONNECTOR_REPORT, ConnectorReportViewImpl.class);
        service.register(DISPLAY_CONNECTOR_INSTANCE, DisplayConnectorInstanceViewImpl.class);
        service.register(ADD_CONNECTOR_MAPPING, AddEditMappingViewImpl.class);
        service.register(EDIT_CONNECTOR_MAPPING, AddEditMappingViewImpl.class);
        service.register(DISPLAY_CONNECTOR_MAPPINGS, DisplayConnectorMappingsViewImpl.class);
        service.register(EDIT_CONNECTOR_INSTANCE, EditConnectorInstanceViewImpl.class);
        service.register(LIST_CONNECTOR_INSTANCES, ListConnectorInstancesViewImpl.class);
        service.register(WIZARD_CONNECTOR_INSTANCE, WizardConnectorInstanceViewImpl.class);
	}

	private static void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(CONNECTORS, CONNECTORS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(ESViews.class).listConnectorInstances();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_CONNECTORS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Inactive(SEARCH_ENGINE, SEARCH_ENGINE_ICON));
	}

	private static void configureHomeFragments(NavigationConfig config) {
		if (!config.hasNavigationItem(HomeView.TABS, TAXONOMIES)) {
			config.add(HomeView.TABS, new RecordTree(TAXONOMIES) {
				private int defaultTab;

				@Override
				public List<RecordLazyTreeDataProvider> getDataProviders(ModelLayerFactory modelLayerFactory,
						SessionContext sessionContext) {
					TaxonomyTabSheet tabSheet = new TaxonomyTabSheet(modelLayerFactory, sessionContext);
					defaultTab = tabSheet.getDefaultTab();
					return tabSheet.getDataProviders();
				}

				@Override
				public BaseContextMenu getContextMenu() {
					return new BaseContextMenu();
				}
			});
		}
	}
}
