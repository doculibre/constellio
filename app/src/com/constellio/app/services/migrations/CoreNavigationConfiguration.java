package com.constellio.app.services.migrations;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.app.ui.pages.viewGroups.TrashViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.CredentialUserPermissionChecker;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.FontAwesome;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

public class CoreNavigationConfiguration implements Serializable {

	public static final String CONFIG = "config";
	public static final String CONFIG_ICON = "images/icons/config/configuration.png";
	public static final String LDAP_CONFIG = "ldapConfig";
	public static final String LDAP_CONFIG_ICON = "images/icons/config/address_book3.png";
	public static final String GROUPS = "groups";
	public static final String GROUPS_ICON = "images/icons/config/group.png";
	public static final String USERS = "users";
	public static final String USERS_ICON = "images/icons/config/user.png";
	public static final String COLLECTIONS = "collections";
	public static final String COLLECTIONS_ICON = "images/icons/config/collections.png";
	public static final String PLUGINS = "plugins";
	public static final String PLUGINS_ICON = "images/icons/config/module.png";
	public static final String IMPORT_USERS = "importUsers";
	public static final String IMPORT_USERS_ICON = "images/icons/config/import-users.png";
	public static final String EXPORT = "export";
	public static final String EXPORT_ICONS = "images/icons/config/export.png";
	public static final String IMPORT_AUTHORIZATIONS = "importAuthorizations";
	public static final String IMPORT_GROUPS = "importGroups";
	public static final String IMPORT_AUTHORIZATIONS_ICON = "images/icons/config/import-authorizations.png";
	public static final String IMPORT_GROUPS_ICON = "images/icons/config/import-groups.png";
	public static final String BIG_DATA = "bigData";
	public static final String BIG_DATA_ICON = "images/icons/config/big-data.png";
	public static final String UPDATE_CENTER = "updateCenter";
	public static final String UPDATE_CENTER_ICON = "images/icons/config/update-center.png";
	public static final String EMAIL_SERVER = "emailServer";
	public static final String EMAIL_SERVER_ICON = "images/icons/config/mail_server.png";

	public static final String TRASH_BIN = "trashBin";
	public static final String TRASH_BIN_ICON = "images/icons/config/garbage.png";

	public static final String TAXONOMIES = "taxonomies";
	public static final String TAXONOMIES_ICON = "images/icons/config/taxonomy.png";
	public static final String VALUE_DOMAINS = "valueDomains";
	public static final String VALUE_DOMAINS_ICON = "images/icons/config/value-domain.png";
	public static final String FACET_CONFIGURATION = "listFacetConfiguration";
	public static final String FACET_CONFIGURATION_ICON = "images/icons/config/funnel.png";
	public static final String METADATA_SCHEMAS = "metadataSchemas";
	public static final String METADATA_SCHEMAS_ICON = "images/icons/config/metadata.png";
	public static final String SECURITY = "security";
	public static final String SECURITY_ICON = "images/icons/config/collection-security.png";
	public static final String ROLES = "roles";
	public static final String ROLES_ICON = "images/icons/config/crown.png";
	public static final String DATA_EXTRACTOR = "dataExtractor";
	public static final String DATA_EXTRACTOR_ICON = "images/icons/config/metadata-extract.png";
	public static final String IMPORT_RECORDS = "importRecords";
	public static final String IMPORT_RECORDS_ICON = "images/icons/config/import.png";
	public static final String IMPORT_SCHEMA_TYPES = "importSchemaTypes";
	public static final String IMPORT_SCHEMA_TYPES_ICON = "images/icons/config/import-metadata.png";
	public static final String IMPORT_SETTINGS = "importSettings";
	public static final String IMPORT_SETTINGS_ICON = "images/icons/config/import-settings.png";
	public static final String SEARCH_BOOST_BY_METADATA = "searchBoostByMetadata";
	public static final String SEARCH_BOOST_BY_METADATA_ICON = "images/icons/config/boost-metadata-search.png";
	public static final String SEARCH_BOOST_BY_QUERY = "searchBoostByQuery";
	public static final String SEARCH_BOOST_BY_QUERY_ICON = "images/icons/config/boost-text-search.png";
	public static final String PRINTABLE_MANAGEMENT = "printableManagement";
	public static final String PRINTABLE_MANAGEMENT_ICON = "images/icons/config/printer.png";

	public static final String ADMIN_MODULE = "adminModule";
	public static final String HOME = "home";
	public static final String TRASH = "trash";
	public static final String BATCH_PROCESSES = "batchProcesses";

	public static final String SYSTEM_CHECK = "systemCheck";
	public static final String SYSTEM_CHECK_ICON = "images/icons/config/system-check.png";

	public static final String TEMPORARY_RECORDS = "temporaryRecords";
	public static final String TEMPORARY_RECORDS_ICON = "images/icons/config/hourglass.png";

	public static final String SEARCH_CONFIG = "searchConfig";
	public static final String SEARCH_CONFIG_ICON = "images/icons/config/configuration-search.png";

	public void configureNavigation(NavigationConfig config) {
		configureHeaderActionMenu(config);
		configureSystemAdmin(config);
		configureCollectionAdmin(config);
		configureMainLayoutNavigation(config);
	}

	private static void configureHeaderActionMenu(NavigationConfig config) {
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(BATCH_PROCESSES) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().batchProcesses();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return ComponentState.visibleIf(user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).onSomething());
			}
		});
	}

	private void configureSystemAdmin(NavigationConfig config) {
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(CONFIG, CONFIG_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().configManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION));
			}
		});

		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(LDAP_CONFIG, LDAP_CONFIG_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().ldapConfigManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_LDAP));
			}
		});

		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(GROUPS, GROUPS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listGlobalGroups();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(USERS, USERS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listUserCredentials();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(COLLECTIONS, COLLECTIONS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().manageCollections();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_COLLECTIONS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(PLUGINS, PLUGINS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().pluginManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(IMPORT_USERS, IMPORT_USERS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importUsers();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(IMPORT_GROUPS, IMPORT_GROUPS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importGroups();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(IMPORT_SETTINGS, IMPORT_SETTINGS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importSettings();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
			}
		});

		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(EXPORT, EXPORT_ICONS) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().exporter();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
			}
		});

		//		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Inactive(BIG_DATA, BIG_DATA_ICON));

		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(UPDATE_CENTER, UPDATE_CENTER_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().updateManager();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_UPDATES));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(SYSTEM_CHECK, SYSTEM_CHECK_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().systemCheck();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
				return visibleIf(userServices.getUser(user.getUsername()).isSystemAdmin()
								 || userServices.has(user).allGlobalPermissionsInAnyCollection(
						CorePermissions.MANAGE_SYSTEM_COLLECTIONS, CorePermissions.MANAGE_SECURITY));
			}
		});
	}

	private void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(TAXONOMIES, TAXONOMIES_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listTaxonomies();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_TAXONOMIES).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(VALUE_DOMAINS, VALUE_DOMAINS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listValueDomains();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_VALUELIST).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(SEARCH_CONFIG, SEARCH_CONFIG_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().searchConfiguration();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.hasAny(
						CorePermissions.MANAGE_SEARCH_BOOST,
						CorePermissions.ACCESS_SEARCH_CAPSULE,
						CorePermissions.MANAGE_FACETS,
						CorePermissions.EXCLUDE_AND_RAISE_SEARCH_RESULT,
						CorePermissions.MANAGE_SYNONYMS,
						CorePermissions.DELETE_CORRECTION_SUGGESTION
				).globally());
			}

		});

		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(PRINTABLE_MANAGEMENT, PRINTABLE_MANAGEMENT_ICON) {

			@Override
			public void activate(Navigation navigate) {
				navigate.to().viewReport();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(user.hasAny(CorePermissions.MANAGE_LABELS, CorePermissions.MANAGE_EXCEL_REPORT,
						CorePermissions.MANAGE_PRINTABLE_REPORT).globally());
			}
		});

		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(METADATA_SCHEMAS, METADATA_SCHEMAS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listSchemaTypes();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(SECURITY, SECURITY_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().collectionSecurity();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});

		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(ROLES, ROLES_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().permissionManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(EMAIL_SERVER, EMAIL_SERVER_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().emailServerManagement();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_EMAIL_SERVER).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(DATA_EXTRACTOR, DATA_EXTRACTOR_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listMetadataExtractors();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_METADATAEXTRACTOR).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(IMPORT_RECORDS, IMPORT_RECORDS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importRecords();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(IMPORT_SCHEMA_TYPES, IMPORT_SCHEMA_TYPES_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importSchemaTypes();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(IMPORT_AUTHORIZATIONS, IMPORT_AUTHORIZATIONS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().importAuthorizations();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				CredentialUserPermissionChecker userHas = appLayerFactory.getModelLayerFactory().newUserServices()
						.has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
			}
		});
		//		config.add(AdminView.COLLECTION_SECTION,
		//				new NavigationItem.Active(SEARCH_BOOST_BY_METADATA, SEARCH_BOOST_BY_METADATA_ICON) {
		//					@Override
		//					public void activate(Navigation navigate) {
		//						navigate.to().searchBoostByMetadatas();
		//					}
		//
		//					@Override
		//					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
		//						return visibleIf(user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally());
		//					}
		//				});
		//		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(SEARCH_BOOST_BY_QUERY, SEARCH_BOOST_BY_QUERY_ICON) {
		//			@Override
		//			public void activate(Navigation navigate) {
		//				navigate.to().searchBoostByQuerys();
		//			}
		//
		//			@Override
		//			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
		//				return visibleIf(user.has(CorePermissions.MANAGE_SEARCH_BOOST).globally());
		//			}
		//		});

		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(TEMPORARY_RECORDS, TEMPORARY_RECORDS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to().listTemporaryRecords();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
				return visibleIf(userServices.getUser(user.getUsername()).isSystemAdmin()
								 || user.hasAny(CorePermissions.ACCESS_TEMPORARY_RECORD, CorePermissions.SEE_ALL_TEMPORARY_RECORD)
										 .globally());
			}
		});
	}

	private void configureMainLayoutNavigation(NavigationConfig config) {
		//		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
		//				new NavigationItem.Active(null, null, PrintableViewGroup.class) {
		//					@Override
		//					public void activate(Navigation navigate) {
		//						navigate.to().viewReport();
		//					}
		//
		//					@Override
		//					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
		//						return ComponentState.INVISIBLE;
		//					}
		//				});
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(HOME, FontAwesome.HOME, RecordsManagementViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						navigate.to().home();
					}

					@Override
					public int getOrderValue() {
						return 10;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						return ComponentState.ENABLED;
					}
				});

		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(ADMIN_MODULE, FontAwesome.GEARS, AdminViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						navigate.to().adminModule();
					}

					@Override
					public int getOrderValue() {
						return 60;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						List<String> permissions = new ArrayList<>();
						permissions.addAll(CorePermissions.COLLECTION_MANAGEMENT_PERMISSIONS);
						permissions.addAll(RMPermissionsTo.RM_COLLECTION_MANAGEMENT_PERMISSIONS);
						permissions.add(TasksPermissionsTo.MANAGE_WORKFLOWS);

						boolean canManageCollection = user.hasAny(permissions).globally();

						UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
						boolean canManageSystem = userServices.has(user.getUsername())
								.anyGlobalPermissionInAnyCollection(CorePermissions.SYSTEM_MANAGEMENT_PERMISSIONS);
						return visibleIf(canManageCollection || canManageSystem);
					}
				});

		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION,
				new NavigationItem.Active(TRASH, FontAwesome.TRASH, TrashViewGroup.class) {
					@Override
					public void activate(Navigation navigate) {
						navigate.to().trash();
					}

					@Override
					public int getOrderValue() {
						return 100;
					}

					@Override
					public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
						return visibleIf(user.has(CorePermissions.MANAGE_TRASH).globally());
					}
				});
	}
}
