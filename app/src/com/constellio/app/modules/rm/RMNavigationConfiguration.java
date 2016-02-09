package com.constellio.app.modules.rm;

import static com.constellio.app.ui.framework.components.ComponentState.enabledIf;
import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.TreeListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTreeItemEvent;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.ui.components.contextmenu.DocumentContextMenuImpl;
import com.constellio.app.modules.rm.ui.pages.home.CheckedOutDocumentsTable;
import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.app.ui.pages.home.RecentItemProvider;
import com.constellio.app.ui.pages.home.TaxonomyTabSheet;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.viewGroups.CartViewGroup;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;
import com.constellio.app.ui.pages.viewGroups.UserDocumentsViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RMNavigationConfiguration implements Serializable {
	public static final String ADD_FOLDER = "addFolder";
	public static final String ADD_DOCUMENT = "addDocument";

	public static final String LAST_VIEWED_FOLDERS = "lastViewedFolders";
	public static final String LAST_VIEWED_DOCUMENTS = "lastViewedDocuments";
	public static final String CHECKED_OUT_DOCUMENTS = "checkedOutDocuments";
	public static final String TAXONOMIES = "taxonomies";

	public static final String UNIFORM_SUBDIVISIONS = "uniformSubdivisions";
	public static final String UNIFORM_SUBDIVISIONS_ICON = "images/icons/config/uniform-subdivision.png";
	public static final String RETENTION_CALENDAR = "retentionCalendar";
	public static final String RETENTION_CALENDAR_ICON = "images/icons/config/calendar.png";

	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String ADMINISTRATIVE_UNIT_ICON = "images/icons/config/administrative-unit.png";
	public static final String CLASSIFICATION_PLAN = "classificationPlan";
	public static final String CLASSIFICATION_PLAN_ICON = "images/icons/config/classification-plan.png";

	public static final String ARCHIVES_MANAGEMENT = "archivesManagement";
	public static final String USER_DOCUMENTS = "userDocuments";
	public static final String CART = "cart";
	public static final String LOGS = "logs";

	public void configureNavigation(NavigationConfig config) {
		configureHomeActionMenu(config);
		configureHomeFragments(config);
		configureCollectionAdmin(config);
		configureMainLayoutNavigation(config);
	}

	private void configureHomeActionMenu(NavigationConfig config) {
		config.add(HomeView.ACTION_MENU, new NavigationItem.Active(ADD_FOLDER) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.addFolder(null, null);
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_FOLDERS).onSomething());
			}
		});
		config.add(HomeView.ACTION_MENU, new NavigationItem.Active(ADD_DOCUMENT) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.addDocument(null, null);
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_DOCUMENTS).onSomething());
			}
		});
	}

	private void configureHomeFragments(NavigationConfig config) {
		config.add(HomeView.TABS, new RecentItemTable(LAST_VIEWED_FOLDERS) {
			@Override
			public List<RecentItem> getItems(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
				return new RecentItemProvider(modelLayerFactory, sessionContext, Folder.SCHEMA_TYPE, "view_folder").getItems();
			}
		});
		config.add(HomeView.TABS, new RecentItemTable(LAST_VIEWED_DOCUMENTS) {
			@Override
			public List<RecentItem> getItems(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
				return new RecentItemProvider(modelLayerFactory, sessionContext, Document.SCHEMA_TYPE, "view_document")
						.getItems();
			}
		});
		config.add(HomeView.TABS, new RecordTable(CHECKED_OUT_DOCUMENTS) {
			@Override
			public RecordVODataProvider getDataProvider(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
				return new CheckedOutDocumentsTable(modelLayerFactory, sessionContext).getDataProvider();
			}
		});
		RecordTree taxonomyTree = new RecordTree(TAXONOMIES) {
			private int defaultTab;

			@Override
			public List<RecordLazyTreeDataProvider> getDataProviders(ModelLayerFactory modelLayerFactory,
					SessionContext sessionContext) {
				TaxonomyTabSheet tabSheet = new TaxonomyTabSheet(modelLayerFactory, sessionContext);
				defaultTab = tabSheet.getDefaultTab();
				return tabSheet.getDataProviders();
			}

			@Override
			public int getDefaultTab() {
				return defaultTab;
			}

			@Override
			public BaseContextMenu getContextMenu() {
				final DocumentContextMenuImpl menu = new DocumentContextMenuImpl();
				menu.addContextMenuTreeListener(new TreeListener() {
					@Override
					public void onContextMenuOpenFromTreeItem(ContextMenuOpenedOnTreeItemEvent event) {
						String recordId = (String) event.getItemId();
						menu.openFor(recordId);
					}
				});
				return menu;
			}
		};
		if (!config.hasNavigationItem(HomeView.TABS, TAXONOMIES)) {
			config.add(HomeView.TABS, taxonomyTree);
		} else {
			config.replace(HomeView.TABS, taxonomyTree);
		}
	}

	private void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(ADMINISTRATIVE_UNIT, ADMINISTRATIVE_UNIT_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.taxonomyManagement(RMTaxonomies.ADMINISTRATIVE_UNITS);
			}

			@Override
			public int getOrderValue() {
				return 1;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(CLASSIFICATION_PLAN, CLASSIFICATION_PLAN_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.taxonomyManagement(RMTaxonomies.CLASSIFICATION_PLAN);
			}

			@Override
			public int getOrderValue() {
				return 2;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(UNIFORM_SUBDIVISIONS, UNIFORM_SUBDIVISIONS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listSchemaRecords(UniformSubdivision.DEFAULT_SCHEMA);
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(RETENTION_CALENDAR, RETENTION_CALENDAR_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listRetentionRules();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally());
			}
		});
		config.replace(AdminView.COLLECTION_SECTION,
				new NavigationItem.Decorator(getTaxonomyItem(config)) {
					@Override
					public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
						return visibleIf(item.getStateFor(user, modelLayerFactory).isVisible() ||
								user.has(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally());
					}

					@Override
					public int getOrderValue() {
						return 999;
					}
				}
		);
	}

	private void configureMainLayoutNavigation(NavigationConfig config) {
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(ARCHIVES_MANAGEMENT, ArchivesManagementViewGroup.class) {
					@Override
					public void activate(ConstellioNavigator navigateTo) {
						navigateTo.archivesManagement();
					}

					@Override
					public int getOrderValue() {
						return 20;
					}

					@Override
					public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
						DecommissioningSecurityService service = new DecommissioningSecurityService(
								user.getCollection(), modelLayerFactory);
						return visibleIf(service.hasAccessToDecommissioningMainPage(user) ||
								user.has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething() ||
								user.has(RMPermissionsTo.MANAGE_REPORTS).onSomething());
					}
				});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(USER_DOCUMENTS, UserDocumentsViewGroup.class) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listUserDocuments();
			}

			@Override
			public int getOrderValue() {
				return 40;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return ComponentState.ENABLED;
			}
		});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(CART, CartViewGroup.class) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.cart();
			}

			@Override
			public int getOrderValue() {
				return 45;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return ComponentState.ENABLED;
			}
		});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(LOGS, LogsViewGroup.class) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listEvents();
			}

			@Override
			public int getOrderValue() {
				return 50;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.VIEW_EVENTS).globally());
			}
		});
	}

	private NavigationItem getTaxonomyItem(NavigationConfig config) {
		return config.getNavigationItem(AdminView.COLLECTION_SECTION, CoreNavigationConfiguration.TAXONOMIES);
	}
}
