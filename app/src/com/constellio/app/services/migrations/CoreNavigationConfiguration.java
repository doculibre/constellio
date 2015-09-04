/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.services.migrations;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.ui.pages.viewGroups.RecordsManagementViewGroup;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.CredentialUserPermissionChecker;
import com.constellio.model.services.users.UserServices;

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
	public static final String MODULES = "modules";
	public static final String MODULES_ICON = "images/icons/config/module.png";
	public static final String IMPORT_USERS = "importUsers";
	public static final String IMPORT_USERS_ICON = "images/icons/config/import-users.png";
	public static final String EXPORT = "export";
	public static final String EXPORT_ICONS = "images/icons/config/export.png";
	public static final String BIG_DATA = "bigData";
	public static final String BIG_DATA_ICON = "images/icons/config/big-data.png";
	public static final String UPDATE_CENTER = "updateCenter";
	public static final String UPDATE_CENTER_ICON = "images/icons/config/update-center.png";

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
	public static final String TRASH_BIN = "trashBin";
	public static final String TRASH_BIN_ICON = "images/icons/config/garbage.png";
	public static final String EMAIL_SERVER = "emailServer";
	public static final String EMAIL_SERVER_ICON = "images/icons/config/mail_server.png";

	public static final String ADMIN_MODULE = "adminModule";
	public static final String RECORDS_MANAGEMENT = "recordsManagement";

	public void configureNavigation(NavigationConfig config) {
		configureSystemAdmin(config);
		configureCollectionAdmin(config);
		configureMainLayoutNavigation(config);
	}

	private void configureSystemAdmin(NavigationConfig config) {
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(CONFIG, CONFIG_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.configManagement();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(LDAP_CONFIG, LDAP_CONFIG_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.ldapConfigManagement();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_LDAP));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(GROUPS, GROUPS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listGlobalGroups();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(USERS, USERS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listUserCredentials();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(COLLECTIONS, COLLECTIONS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.manageCollections();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_COLLECTIONS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Inactive(MODULES, MODULES_ICON));
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(IMPORT_USERS, IMPORT_USERS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.importUsers();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_USERS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(EXPORT, EXPORT_ICONS) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.exporter();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS));
			}
		});
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Inactive(BIG_DATA, BIG_DATA_ICON));
		config.add(AdminView.SYSTEM_SECTION, new NavigationItem.Active(UPDATE_CENTER, UPDATE_CENTER_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.updateManager();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				CredentialUserPermissionChecker userHas = modelLayerFactory.newUserServices().has(user.getUsername());
				return visibleIf(userHas.globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_UPDATES));
			}
		});
	}

	private void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(TAXONOMIES, TAXONOMIES_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listTaxonomies();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.hasAny(CorePermissions.MANAGE_TAXONOMIES, CorePermissions.MANAGE_SECURITY).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(VALUE_DOMAINS, VALUE_DOMAINS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listValueDomains();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_VALUELIST).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(FACET_CONFIGURATION, FACET_CONFIGURATION_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listFacetConfiguration();
			}

			//TODO changer pour permission
			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_VALUELIST).globally());
			}

		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(METADATA_SCHEMAS, METADATA_SCHEMAS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.listSchemaTypes();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(SECURITY, SECURITY_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.collectionSecurity();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(ROLES, ROLES_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.permissionManagement();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SECURITY).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(EMAIL_SERVER, EMAIL_SERVER_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.emailServerManagement();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_EMAIL_SERVER).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Inactive(DATA_EXTRACTOR, DATA_EXTRACTOR_ICON));
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(IMPORT_RECORDS, IMPORT_RECORDS_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.importRecords();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(IMPORT_SCHEMA_TYPES, IMPORT_SCHEMA_TYPES_ICON) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.importSchemaTypes();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally());
			}
		});
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Inactive(TRASH_BIN, TRASH_BIN_ICON));
	}

	private void configureMainLayoutNavigation(NavigationConfig config) {
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION1,
				new NavigationItem.Active(RECORDS_MANAGEMENT, RecordsManagementViewGroup.class) {
					@Override
					public void activate(ConstellioNavigator navigateTo) {
						navigateTo.home();
					}

					@Override
					public int getOrderValue() {
						return 10;
					}

					@Override
					public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
						return ComponentState.ENABLED;
					}
				});

		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION2, new NavigationItem.Active(ADMIN_MODULE, AdminViewGroup.class) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.adminModule();
			}

			@Override
			public int getOrderValue() {
				return 60;
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				List<String> permissions = new ArrayList<>();
				permissions.addAll(CorePermissions.COLLECTION_MANAGEMENT_PERMISSIONS);
				permissions.addAll(RMPermissionsTo.RM_COLLECTION_MANAGEMENT_PERMISSIONS);

				boolean canManageCollection = user.hasAny(permissions).globally();

				UserServices userServices = modelLayerFactory.newUserServices();
				boolean canManageSystem = userServices.has(user.getUsername())
						.anyGlobalPermissionInAnyCollection(CorePermissions.SYSTEM_MANAGEMENT_PERMISSIONS);
				return visibleIf(canManageCollection || canManageSystem);
			}
		});
	}

}
