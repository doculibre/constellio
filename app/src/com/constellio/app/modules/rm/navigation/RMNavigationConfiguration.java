package com.constellio.app.modules.rm.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.NavigationItem.Active;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.entities.navigation.PageItem.SharedItemsTables;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.ui.components.contextmenu.DocumentContextMenuImpl;
import com.constellio.app.modules.rm.ui.pages.agent.AgentRequestViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.AgentSetupViewImpl;
import com.constellio.app.modules.rm.ui.pages.agent.ListAgentLogsViewImpl;
import com.constellio.app.modules.rm.ui.pages.cart.CartViewImpl;
import com.constellio.app.modules.rm.ui.pages.cart.CartsListViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInFilingSpaceViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddExistingContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddNewContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DocumentDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.EditDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.AddEditDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.email.AddEmailAndEmailAttachmentsToFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.email.AddEmailAttachmentsToFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.AddEditFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.home.CheckedOutDocumentsTable;
import com.constellio.app.modules.rm.ui.pages.home.SharedDocumentsAndFoldersProvider;
import com.constellio.app.modules.rm.ui.pages.management.ArchiveManagementViewImpl;
import com.constellio.app.modules.rm.ui.pages.personalspace.PersonnalSpaceView;
import com.constellio.app.modules.rm.ui.pages.reports.RMReportsViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.AddEditRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.DisplayRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.ListRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.SearchRetentionRulesViewImpl;
import com.constellio.app.modules.rm.ui.pages.shareManagement.ShareContentListViewImpl;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.constellio.app.modules.rm.ui.pages.viewGroups.AgentViewGroup;
import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.app.ui.pages.home.RecentItemProvider;
import com.constellio.app.ui.pages.home.TaxonomyTabSheet;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.viewGroups.CartViewGroup;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.TableListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.TreeListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTreeItemEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.framework.components.ComponentState.enabledIf;
import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

public class RMNavigationConfiguration implements Serializable {

	public static final String NEW_DOCUMENT = "newDocument";
	public static final String ADD_FOLDER = "addFolder";
	public static final String ADD_SUB_FOLDER = "addSubFolder";
	public static final String ADD_DOCUMENT = "addDocument";
	public static final String ADD_CONTAINER_RECORD = "addContainerRecord";

	public static final String LAST_VIEWED_FOLDERS = "lastViewedFolders";
	public static final String LAST_VIEWED_DOCUMENTS = "lastViewedDocuments";
	public static final String CHECKED_OUT_DOCUMENTS = "checkedOutDocuments";
	public static final String SHARED_ITEMS = "sharedDocuments";
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
	public static final String LIST_CARTS = "listCarts";
	public static final String MY_CART = "myCart";
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
	public static final String DUPLICATE_FOLDER = "duplicateFolder";
	public static final String DISPLAY_DOCUMENT = "displayDocument";
	public static final String ADD_EMAIL_ATTACHMENTS_TO_FOLDER = "addEmailAttachmentsToFolder";
	public static final String ADD_EMAIL_AND_EMAIL_ATTACHMENTS_TO_FOLDER = "addEmailAndEmailAttachmentsToFolder";
	public static final String EDIT_FOLDER = "editFolder";
	public static final String DISPLAY_FOLDER = "displayFolder";
	public static final String ADD_RETENTION_RULE = "addRetentionRule";
	public static final String EDIT_RETENTION_RULE = "editRetentionRule";
	public static final String DISPLAY_RETENTION_RULE = "displayRetentionRule";
	public static final String LIST_RETENTION_RULES = "listRetentionRules";
	public static final String RETENTION_RULES_SEARCH = "retentionRuleSearch";
	public static final String LIST_USER_DOCUMENTS = "listUserDocuments";
	public static final String LIST_USER_DOCUMENTS_ICON = "images/icons/config/briefcase.png";
	public static final String SHARE_MANAGEMENT = "shareManagement";
	public static final String SHARES = "shares";
	public static final String SHARES_ICON = "images/icons/config/paper_jet2.png";


	public static void configureNavigation(NavigationConfig config) {
		configureHeaderActionMenu(config);
		configureHomeFragments(config);
		configurePersonalSpace(config);
		configureCollectionAdmin(config);
		configureMainLayoutNavigation(config);
	}

	public static void configureNavigation(NavigatorConfigurationService service) {
		service.register(REPORTS, RMReportsViewImpl.class);
		service.register(REQUEST_AGENT, AgentRequestViewImpl.class);
		service.register(AGENT_SETUP, AgentSetupViewImpl.class);
		service.register(LIST_AGENT_LOGS, ListAgentLogsViewImpl.class);
		service.register(CART, CartViewImpl.class);
		service.register(LIST_CARTS, CartsListViewImpl.class);
		service.register(MY_CART, CartViewImpl.class);
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
		service.register(ADD_EMAIL_AND_EMAIL_ATTACHMENTS_TO_FOLDER, AddEmailAndEmailAttachmentsToFolderViewImpl.class);
		service.register(ADD_FOLDER, AddEditFolderViewImpl.class);
		service.register(EDIT_FOLDER, AddEditFolderViewImpl.class);
		service.register(DUPLICATE_FOLDER, AddEditFolderViewImpl.class);
		service.register(DISPLAY_FOLDER, DisplayFolderViewImpl.class);
		service.register(ARCHIVES_MANAGEMENT, ArchiveManagementViewImpl.class);
		service.register(ADD_RETENTION_RULE, AddEditRetentionRuleViewImpl.class);
		service.register(EDIT_RETENTION_RULE, AddEditRetentionRuleViewImpl.class);
		service.register(DISPLAY_RETENTION_RULE, DisplayRetentionRuleViewImpl.class);
		service.register(LIST_RETENTION_RULES, ListRetentionRulesViewImpl.class);
		service.register(RETENTION_RULES_SEARCH, SearchRetentionRulesViewImpl.class);
		service.register(LIST_USER_DOCUMENTS, ListUserDocumentsViewImpl.class);
		service.register(SHARE_MANAGEMENT, ShareContentListViewImpl.class);

	}

	private static void configureHeaderActionMenu(NavigationConfig config) {
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(ADD_FOLDER) {
			@Override
			public void activate(Navigation navigate) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				if (currentView instanceof DisplayFolderView) {
					DisplayFolderView displayFolderView = (DisplayFolderView) currentView;
					String parentFolderId = displayFolderView.getRecord().getId();
					navigate.to(RMViews.class).addFolder(parentFolderId);
				} else {
					navigate.to(RMViews.class).addFolder();
				}
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_FOLDERS).onSomething());
			}

			@Override
			public void viewChanged(BaseView oldView, BaseView newView) {
				if (ADD_FOLDER.equals(getCode()) && newView instanceof DisplayFolderView) {
					setCode(ADD_SUB_FOLDER);
				} else if (!ADD_FOLDER.equals(getCode()) && !(newView instanceof DisplayFolderView)) {
					setCode(ADD_FOLDER);
				}
			}
		}, 0);
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(NEW_DOCUMENT) {
			@Override
			public void activate(Navigation navigate) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				if (currentView instanceof DisplayFolderView) {
					DisplayFolderView displayFolderView = (DisplayFolderView) currentView;
					String folderId = displayFolderView.getRecord().getId();
					navigate.to(RMViews.class).newDocument(folderId);
				} else {
					navigate.to(RMViews.class).newDocument();
				}
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_DOCUMENTS).onSomething());
			}
		}, 1);
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(ADD_DOCUMENT) {
			@Override
			public void activate(Navigation navigate) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				if (currentView instanceof DisplayFolderView) {
					DisplayFolderView displayFolderView = (DisplayFolderView) currentView;
					String folderId = displayFolderView.getRecord().getId();
					navigate.to(RMViews.class).addDocument(folderId);
				} else {
					navigate.to(RMViews.class).addDocument();
				}
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.CREATE_DOCUMENTS).onSomething());
			}
		}, 2);

		config.add(ConstellioHeader.ACTION_MENU, new Active(ADD_CONTAINER_RECORD) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).addContainer();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return enabledIf(user.has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething());
			}
		}, 3);
	}

	private static void configureHomeFragments(NavigationConfig config) {
		RecordTree taxonomyTree = new RecordTree(TAXONOMIES) {
			@Override
			public List<RecordLazyTreeDataProvider> getDataProviders(AppLayerFactory appLayerFactory,
																	 SessionContext sessionContext) {
				TaxonomyTabSheet tabSheet = new TaxonomyTabSheet(appLayerFactory.getModelLayerFactory(), sessionContext);
				if (getDefaultDataProvider() == -1) {
					int defaultTab = tabSheet.getDefaultTab();
					setDefaultDataProvider(defaultTab);
				}
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
				menu.addContextMenuTableListener(new TableListener() {
					@Override
					public void onContextMenuOpenFromRow(ContextMenuOpenedOnTableRowEvent event) {
						String recordId = (String) event.getItemId();
						menu.openFor(recordId);
					}

					@Override
					public void onContextMenuOpenFromHeader(ContextMenuOpenedOnTableHeaderEvent event) {
					}

					@Override
					public void onContextMenuOpenFromFooter(ContextMenuOpenedOnTableFooterEvent event) {
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

		config.add(HomeView.TABS, new RecentItemTable(LAST_VIEWED_FOLDERS, Folder.SCHEMA_TYPE) {
			@Override
			public List<RecentItem> getItems(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
				return new RecentItemProvider(appLayerFactory.getModelLayerFactory(), sessionContext, Folder.SCHEMA_TYPE)
						.getItems();
			}
		});
		config.add(HomeView.TABS, new RecentItemTable(LAST_VIEWED_DOCUMENTS, Document.SCHEMA_TYPE) {
			@Override
			public List<RecentItem> getItems(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
				return new RecentItemProvider(appLayerFactory.getModelLayerFactory(), sessionContext, Document.SCHEMA_TYPE)
						.getItems();
			}
		});
		config.addRefreshablePageItem(HomeView.TABS, new RecordTable(CHECKED_OUT_DOCUMENTS) {
			@Override
			public RecordVODataProvider getDataProvider(AppLayerFactory appLayerFactory,
														SessionContext sessionContext) {
				return new CheckedOutDocumentsTable(appLayerFactory, sessionContext).getDataProvider();
			}
		});
		config.add(HomeView.TABS, new SharedItemsTables(SHARED_ITEMS){
			@Override
			public Map<String,RecordVODataProvider> getDataProvider(AppLayerFactory appLayerFactory,
															 SessionContext sessionContext) {
				return new SharedDocumentsAndFoldersProvider(appLayerFactory, sessionContext).getDataProviders();
			}
		});
	}

	private static void configurePersonalSpace(NavigationConfig config) {
		config.add(PersonnalSpaceView.PERSONAL_SPACE, new NavigationItem.Active(LIST_USER_DOCUMENTS, LIST_USER_DOCUMENTS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).listUserDocuments();
			}

			@Override
			public int getOrderValue() {
				return 1;
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(true);
			}
		});
	}

	private static void configureCollectionAdmin(NavigationConfig config) {
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
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
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
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.hasAny(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN, RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN).onSomething());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(UNIFORM_SUBDIVISIONS, UNIFORM_SUBDIVISIONS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listSchemaRecords(UniformSubdivision.DEFAULT_SCHEMA);
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS).globally()
								 && new RMConfigs(appLayerFactory).areUniformSubdivisionEnabled());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(RETENTION_CALENDAR, RETENTION_CALENDAR_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).listRetentionRules();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.hasAny(RMPermissionsTo.MANAGE_RETENTIONRULE, RMPermissionsTo.CONSULT_RETENTIONRULE).onSomething());
			}
		});
		config.replace(AdminView.COLLECTION_SECTION,
				new NavigationItem.Decorator(getTaxonomyItem(config)) {
					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						return visibleIf(item.getStateFor(user, appLayerFactory).isVisible());
					}

					@Override
					public int getOrderValue() {
						return 999;
					}
				}
		);
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(SHARES, SHARES_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).shareManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});
	}

	private static void configureMainLayoutNavigation(NavigationConfig config) {
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(MY_CART, FontAwesome.STAR, CartViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						String userId = ConstellioUI.getCurrentSessionContext().getCurrentUser().getId();
						navigate.to(RMViews.class).cart(userId);
					}

					@Override
					public int getOrderValue() {
						return 35;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						return visibleIf(user.has(RMPermissionsTo.USE_MY_CART).globally() && !user.has(RMPermissionsTo.USE_GROUP_CART).globally());
					}
				});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(LIST_CARTS, FontAwesome.STAR, CartViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).listCarts();
					}

					@Override
					public int getOrderValue() {
						return 35;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						return visibleIf(user.has(RMPermissionsTo.USE_GROUP_CART).globally());
					}
				});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(ARCHIVES_MANAGEMENT, FontAwesome.ARCHIVE, ArchivesManagementViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						navigate.to(RMViews.class).archiveManagement();
					}

					@Override
					public int getOrderValue() {
						return 40;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						DecommissioningSecurityService service = new DecommissioningSecurityService(
								user.getCollection(), appLayerFactory);
						return visibleIf((service.hasAccessToDecommissioningMainPage(user) ||
										  user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally() ||
										  user.has(RMPermissionsTo.MANAGE_REPORTS).onSomething()) && !ResponsiveUtils.isPhone());
					}
				});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(LOGS, FontAwesome.AREA_CHART, LogsViewGroup.class) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).eventAudit();
			}

			@Override
			public int getOrderValue() {
				return 50;
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.VIEW_EVENTS).onSomething() && !ResponsiveUtils.isPhone());
			}
		});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(AGENT, FontAwesome.LAPTOP, AgentViewGroup.class) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RMViews.class).requestAgent();
			}

			@Override
			public int getOrderValue() {
				return 70;
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
				UserServices userServices = modelLayerFactory.newUserServices();
				SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

				RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);

				String username = user.getUsername();
				UserCredential userCredentials = (UserCredential) userServices.getUser(username);
				AgentStatus agentStatus = userCredentials.getAgentStatus();
				if (agentStatus == AgentStatus.DISABLED && !rmConfigs.isAgentDisabledUntilFirstConnection()) {
					agentStatus = AgentStatus.ENABLED;
				}

				return visibleIf(rmConfigs.isAgentEnabled() && ConstellioAgentUtils.isAgentSupported()
								 && agentStatus == AgentStatus.DISABLED && !ResponsiveUtils.isDesktop());
			}
		});
	}

	private static NavigationItem getTaxonomyItem(NavigationConfig config) {
		return config.getNavigationItem(AdminView.COLLECTION_SECTION, CoreNavigationConfiguration.TAXONOMIES);
	}
}
