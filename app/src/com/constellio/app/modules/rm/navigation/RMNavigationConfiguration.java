package com.constellio.app.modules.rm.navigation;

import static com.constellio.app.ui.framework.components.ComponentState.enabledIf;
import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.agent.AgentRequestViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.AgentSetupViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.ListAgentLogsViewImpl;
import com.constellio.app.modules.rm.ui.pages.cart.CartViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInFilingSpaceViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.*;
import com.constellio.app.modules.rm.ui.pages.document.AddEditDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.email.AddEmailAttachmentsToFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.AddEditFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.management.ArchiveManagementViewImpl;
import com.constellio.app.modules.rm.ui.pages.reports.RMReportsViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.AddEditRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.DisplayRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.ListRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.SearchRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.constellio.app.ui.application.NavigatorConfigurationService;
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
import com.constellio.app.modules.rm.ui.pages.viewGroups.AgentViewGroup;
import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.ui.application.Navigation;
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

	public static final String ARCHIVES_MANAGEMENT = "archiveManagement";
	public static final String USER_DOCUMENTS = "userDocuments";
	public static final String AGENT = "agent";
	public static final String CART = "cart";
	public static final String LOGS = "logs";
    public static final String REPORTS = "reports";
    public static final String REQUEST_AGENT = "requestAgent";
    public static final String AGENT_SETUP = "agentSetup";
    public static final String LIST_AGENT_LOGS = "listAgentLogs";
    public static final String EDIT_CONTAINER = "editContainer";
    public static final String CONTAINERS_BY_ADMIN_UNITS = "containersByAdminUnits";
    public static final String DISPLAY_ADMIN_UNIT_WITH_CONTAINERS = "displayAdminUnitWithContainers";
    public static final String DISPLAY_FILING_SPACE_WITH_CONTAINERS = "displayFilingSpaceWithContainers";
    public static final String DISPLAY_CONTAINER = "displayContainer";
    public static final String DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER = "searchContainerForDecommissioningList";
    public static final String DECOMMISSIONING_LIST_ADD_NEW_CONTAINER = "createContainerForDecommissioningList";
    public static final String DECOMMISSIONING_LIST_BUILDER = "decommissioningListBuilder";
    public static final String DECOMMISSIONING_LIST_DISPLAY = "decommissioningList";
    public static final String DECOMMISSIONING = "decommissioning";
    public static final String DOCUMENT_DECOMMISSIONING_LIST_DISPLAY = "documentDecommissioningList";
    public static final String DECOMMISSIONING_LIST_EDIT = "editDecommissioningList";
    public static final String EDIT_DOCUMENT = "editDocument";
    public static final String DISPLAY_DOCUMENT = "displayDocument";
    public static final String ADD_EMAIL_ATTACHMENTS_TO_FOLDER = "addEmailAttachmentsToFolder";
    public static final String EDIT_FOLDER = "editFolder";
    public static final String DISPLAY_FOLDER = "displayFolder";
    public static final String ADD_RETENTION_RULE = "addRetentionRule";
    public static final String EDIT_RETENTION_RULE = "editRetentionRule";
    public static final String DISPLAY_RETENTION_RULE = "displayRetentionRule";
    public static final String LIST_RETENTION_RULES = "listRetentionRules";
    public static final String RETENTION_RULES_SEARCH = "retentionRuleSearch";
    public static final String LIST_USER_DOCUMENTS = "listUserDocuments";

    public void configureNavigation(NavigationConfig config) {
		configureHomeActionMenu(config);
		configureHomeFragments(config);
		configureCollectionAdmin(config);
		configureMainLayoutNavigation(config);
	}

    public static void configureNavigation(NavigatorConfigurationService service) {
        service.register(REPORTS, RMReportsViewImpl.class);
        service.register(REQUEST_AGENT, AgentRequestViewImpl.class);
        service.register(AGENT_SETUP, AgentSetupViewImpl.class);
        service.register(LIST_AGENT_LOGS, ListAgentLogsViewImpl.class);
        service.register(CART, CartViewImpl.class);
        service.register(EDIT_CONTAINER, AddEditContainerViewImpl.class);
        service.register(CONTAINERS_BY_ADMIN_UNITS, ContainersByAdministrativeUnitsViewImpl.class);
        service.register(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS, ContainersInAdministrativeUnitViewImpl.class);
        service.register(DISPLAY_FILING_SPACE_WITH_CONTAINERS, ContainersInFilingSpaceViewImpl.class);
        service.register(DISPLAY_CONTAINER, DisplayContainerViewImpl.class);
        service.register(DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER, AddExistingContainerViewImpl.class);
        service.register(DECOMMISSIONING_LIST_ADD_NEW_CONTAINER, AddNewContainerViewImpl.class);
        service.register(DECOMMISSIONING_LIST_BUILDER, DecommissioningBuilderViewImpl.class);
        service.register(DECOMMISSIONING_LIST_DISPLAY, DecommissioningListViewImpl.class);
        service.register(DECOMMISSIONING, DecommissioningMainViewImpl.class);
        service.register(DOCUMENT_DECOMMISSIONING_LIST_DISPLAY, DocumentDecommissioningListViewImpl.class);
        service.register(DECOMMISSIONING_LIST_EDIT, EditDecommissioningListViewImpl.class);
        service.register(ADD_DOCUMENT, AddEditDocumentViewImpl.class);
        service.register(EDIT_DOCUMENT, AddEditDocumentViewImpl.class);
        service.register(DISPLAY_DOCUMENT, DisplayDocumentViewImpl.class);
        service.register(ADD_EMAIL_ATTACHMENTS_TO_FOLDER, AddEmailAttachmentsToFolderViewImpl.class);
        service.register(ADD_FOLDER, AddEditFolderViewImpl.class);
        service.register(EDIT_FOLDER, AddEditFolderViewImpl.class);
        service.register(DISPLAY_FOLDER, DisplayFolderViewImpl.class);
        service.register(ARCHIVES_MANAGEMENT, ArchiveManagementViewImpl.class);
        service.register(ADD_RETENTION_RULE, AddEditRetentionRuleViewImpl.class);
        service.register(EDIT_RETENTION_RULE, AddEditRetentionRuleViewImpl.class);
        service.register(DISPLAY_RETENTION_RULE, DisplayRetentionRuleViewImpl.class);
        service.register(LIST_RETENTION_RULES, ListRetentionRulesViewImpl.class);
        service.register(RETENTION_RULES_SEARCH, SearchRetentionRulesViewImpl.class);
        service.register(LIST_USER_DOCUMENTS, ListUserDocumentsViewImpl.class);
    }

	private void configureHomeActionMenu(NavigationConfig config) {
		config.add(HomeView.ACTION_MENU, new NavigationItem.Active(ADD_FOLDER) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).addFolder();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_FOLDERS).onSomething());
			}
		});
		config.add(HomeView.ACTION_MENU, new NavigationItem.Active(ADD_DOCUMENT) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).addDocument();
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
			public void activate(Navigation navigate) {
				navigate.to().taxonomyManagement(RMTaxonomies.ADMINISTRATIVE_UNITS);
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
			public void activate(Navigation navigate) {
				navigate.to().taxonomyManagement(RMTaxonomies.CLASSIFICATION_PLAN);
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
			public void activate(Navigation navigate) {
				navigate.to().listSchemaRecords(UniformSubdivision.DEFAULT_SCHEMA);
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(RETENTION_CALENDAR, RETENTION_CALENDAR_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).listRetentionRules();
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
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).archiveManagement();
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
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).listUserDocuments();
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
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).cart();
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
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).eventAudit();
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
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(AGENT, AgentViewGroup.class) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).requestAgent();
			}

			@Override
			public int getOrderValue() {
				return 70;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return ComponentState.ENABLED;
			}
		});
	}

	private NavigationItem getTaxonomyItem(NavigationConfig config) {
		return config.getNavigationItem(AdminView.COLLECTION_SECTION, CoreNavigationConfiguration.TAXONOMIES);
	}
}
