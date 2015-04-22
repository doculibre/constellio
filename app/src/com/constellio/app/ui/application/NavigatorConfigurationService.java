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
package com.constellio.app.ui.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.administrativeUnit.AddEditAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.administrativeUnit.DisplayAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.administrativeUnit.ListAdministrativeUnitsViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInAdministrativeUnitViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersInFilingSpaceViewImpl;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddExistingContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.AddNewContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainViewImpl;
import com.constellio.app.modules.rm.ui.pages.decommissioning.EditDecommissioningListViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.AddEditDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.AddEditFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.ui.pages.home.RecordsManagementViewImpl;
import com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.management.ArchiveManagementViewImpl;
import com.constellio.app.modules.rm.ui.pages.reports.RMReportsViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.AddEditRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.DisplayRetentionRuleViewImpl;
import com.constellio.app.modules.rm.ui.pages.retentionRule.ListRetentionRulesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.pages.collection.CollectionUserViewImpl;
import com.constellio.app.ui.pages.collection.ListCollectionUserViewImpl;
import com.constellio.app.ui.pages.events.BaseEventCategoryViewImpl;
import com.constellio.app.ui.pages.events.EventCategoriesViewImpl;
import com.constellio.app.ui.pages.events.EventViewImpl;
import com.constellio.app.ui.pages.globalGroup.AddEditGlobalGroupViewImpl;
import com.constellio.app.ui.pages.globalGroup.DisplayGlobalGroupViewImpl;
import com.constellio.app.ui.pages.globalGroup.ListGlobalGroupsViewImpl;
import com.constellio.app.ui.pages.management.app.AppManagementView;
import com.constellio.app.ui.pages.management.authorizations.ListContentAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.authorizations.ListPrincipalAuthorizationsViewImpl;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionViewImpl;
import com.constellio.app.ui.pages.management.collections.CollectionManagementViewImpl;
import com.constellio.app.ui.pages.management.configs.ConfigManagementViewImpl;
import com.constellio.app.ui.pages.management.ldap.LDAPConfigManagementViewImpl;
import com.constellio.app.ui.pages.management.permissions.PermissionsManagementViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.AddEditSchemaRecordViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.DisplaySchemaRecordViewImpl;
import com.constellio.app.ui.pages.management.schemaRecords.ListSchemaRecordsViewImpl;
import com.constellio.app.ui.pages.management.schemas.ListSchemaTypeViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.display.DisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.form.FormDisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.group.ListMetadataGroupSchemaTypeViewImpl;
import com.constellio.app.ui.pages.management.schemas.display.search.SearchDisplayConfigViewImpl;
import com.constellio.app.ui.pages.management.schemas.metadata.AddEditMetadataViewImpl;
import com.constellio.app.ui.pages.management.schemas.schema.AddEditSchemaMetadataViewImpl;
import com.constellio.app.ui.pages.management.schemas.schema.AddEditSchemaViewImpl;
import com.constellio.app.ui.pages.management.schemas.type.ListSchemaViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyConceptViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.AddEditTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.ListTaxonomyViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.app.ui.pages.management.updates.UpdateManagerViewImpl;
import com.constellio.app.ui.pages.management.valueDomains.ListValueDomainViewImpl;
import com.constellio.app.ui.pages.profile.ModifyProfileViewImpl;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.constellio.app.ui.pages.user.AddEditUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.DisplayUserCredentialViewImpl;
import com.constellio.app.ui.pages.user.ListUsersCredentialsViewImpl;
import com.constellio.app.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.ClassBasedViewProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

public class NavigatorConfigurationService implements Serializable {
	public static final String HOME = "";
	public static final String APP_MANAGEMENT = "appManagement";
	public static final String SIMPLE_SEARCH = "search";
	public static final String ADVANCED_SEARCH = "advancedSearch";
	public static final String LIST_TAXONOMY = "taxonomies";
	public static final String TAXONOMY_ADD_EDIT = "taxonomyAddEdit";
	public static final String TAXONOMY_MANAGEMENT = "taxonomyManagement";
	public static final String ADMINISTRATIVE_UNIT_LIST = "administrativeUnitList";
	public static final String ADMINISTRATIVE_UNIT_DISPLAY = "administrativeUnitDisplay";
	public static final String ADMINISTRATIVE_UNIT_ADD_EDIT = "administrativeUnitAddEdit";
	public static final String TAXONOMY_CONCEPT_ADD_EDIT = "taxonomyConceptAddEdit";
	public static final String LIST_VALUE_DOMAINS = "valueDomains";
	public static final String GROUP_LIST = "groupList";
	public static final String GROUP_ADD_EDIT = "groupAddEdit";
	public static final String COLLECTION_ADD_EDIT = "collectionAddEdit";
	public static final String GROUP_DISPLAY = "groupDisplay";
	public static final String USER_LIST = "userList";
	public static final String USER_ADD_EDIT = "userAddEdit";
	public static final String EVENTS_LIST = "eventsList";
	public static final String EVENT_CATEGORY = "eventCategory";
	public static final String EVENT_DISPLAY = "eventDisplay";
	public static final String ADD_EDIT_SCHEMA_RECORD = "addSchemaRecord";
	public static final String DISPLAY_SCHEMA_RECORD = "displaySchemaRecord";
	public static final String LIST_SCHEMA_RECORDS = "listSchemaRecords";
	public static final String USER_DISPLAY = "userDisplay";
	public static final String LIST_PRINCIPAL_AUTHORIZATIONS = "principalAuthorizations";
	public static final String LIST_OBJECT_AUTHORIZATIONS = "objectAuthorizations";
	public static final String ADMIN_MODULE = "adminModule";
	public static final String COLLECTION_USER_LIST = "collectionSecurity";
	public static final String COLLECTION_USER = "collectionUser";
	public static final String COLLECTION_USER_ROLES = "collectionUserRoles";
	public static final String COLLECTION_GROUP = "collectionGroup";
	public static final String COLLECTION_GROUP_ROLES = "collectionGroupRoles";
	public static final String CONFIG_MANAGEMENT = "configManagement";
	public static final String REPORTS = "reports";
	public static final String ARCHIVES_MANAGEMENT = "archivesManagement";
	public static final String DECOMMISSIONING = "decommissioning";
	public static final String DECOMMISSIONING_LIST_BUILDER = "decommissioningListBuilder";
	public static final String DECOMMISSIONING_LIST_DISPLAY = "decommissioningList";
	public static final String DECOMMISSIONING_LIST_EDIT = "editDecommissioningList";
	public static final String DECOMMISSIONING_LIST_ADD_NEW_CONTAINER = "createContainerForDecommissioningList";
	public static final String DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER = "searchContainerForDecommissioningList";
	public static final String DISPLAY_SCHEMA_TYPE = "displaySchemaType";
	public static final String DISPLAY_SCHEMA = "displaySchema";
	public static final String ADD_EDIT_SCHEMA = "editSchema";
	public static final String ADD_EDIT_METADATA = "editMetadata";
	public static final String ADD_EDIT_SCHEMA_METADATA = "editSchemaMetadata";
	public static final String LIST_METADATA_GROUP = "listMetadataGroup";
	public static final String RECORDS_MANAGEMENT = "recordsManagement";
	public static final String ADD_FOLDER = "addFolder";
	public static final String EDIT_FOLDER = "editFolder";
	public static final String DISPLAY_FOLDER = "displayFolder";
	public static final String ADD_DOCUMENT = "addDocument";
	public static final String EDIT_DOCUMENT = "editDocument";
	public static final String DISPLAY_DOCUMENT = "displayDocument";
	public static final String EDIT_DISPLAY_FORM = "displayForm";
	public static final String SEARCH_DISPLAY_FORM = "searchDisplayForm";
	public static final String CONTAINERS_BY_ADMIN_UNITS = "containersByAdminUnits";
	public static final String DISPLAY_ADMIN_UNIT_WITH_CONTAINERS = "displayAdminUnitWithContainers";
	public static final String FORM_DISPLAY_FORM = "formDisplayForm";
	public static final String DISPLAY_FILING_SPACE_WITH_CONTAINERS = "displayFilingSpaceWithContainers";
	public static final String DISPLAY_CONTAINER = "displayContainer";
	public static final String ADD_RETENTION_RULE = "addRetentionRule";
	public static final String EDIT_RETENTION_RULE = "editRetentionRule";
	public static final String DISPLAY_RETENTION_RULE = "displayRetentionRule";
	public static final String LIST_RETENTION_RULES = "listRetentionRules";
	public static final String LIST_USER_DOCUMENTS = "listUserDocuments";
	public static final String PERMISSION_MANAGEMENT = "permissionManagement";
	public static final String COLLECTION_MANAGEMENT = "listCollections";
	public static final String LIST_ONGLET = "listOnglet";
	public static final String MODIFY_PROFILE = "modifyProfile";
	public static final String UPDATE_MANAGER = "updateManager";
	public static final String LDAP_CONFIG_MANAGEMENT = "ldapConfigManagement";

	private List<ViewProvider> viewProviders = new ArrayList<>();

	public NavigatorConfigurationService() {
		//		viewProviders.add(new ClassBasedViewProvider(SIMPLE_SEARCH, SimpleSearchViewImpl.class));
		//		viewProviders.add(new ClassBasedViewProvider(ADVANCED_SEARCH, AdvancedSearchViewImpl.class));
		viewProviders.add(new OneInstanceViewProvider(SIMPLE_SEARCH) {
			@Override
			protected View newView(String viewName) {
				return new SimpleSearchViewImpl();
			}
		});
		viewProviders.add(new OneInstanceViewProvider(ADVANCED_SEARCH) {
			@Override
			protected View newView(String viewName) {
				return new AdvancedSearchViewImpl();
			}
		});

		viewProviders.add(new ClassBasedViewProvider(HOME, RecordsManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(APP_MANAGEMENT, AppManagementView.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_MANAGEMENT, TaxonomyManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADMINISTRATIVE_UNIT_LIST, ListAdministrativeUnitsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADMINISTRATIVE_UNIT_DISPLAY, DisplayAdministrativeUnitViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADMINISTRATIVE_UNIT_ADD_EDIT, AddEditAdministrativeUnitViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_CONCEPT_ADD_EDIT, AddEditTaxonomyConceptViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_LIST, ListGlobalGroupsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_ADD_EDIT, AddEditGlobalGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(GROUP_DISPLAY, DisplayGlobalGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_ADD_EDIT, AddEditUserCredentialViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_DISPLAY, DisplayUserCredentialViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(USER_LIST, ListUsersCredentialsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA_RECORD, AddEditSchemaRecordViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA_RECORD, DisplaySchemaRecordViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_SCHEMA_RECORDS, ListSchemaRecordsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA_TYPE, ListSchemaTypeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_SCHEMA, ListSchemaViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA, AddEditSchemaViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_METADATA, AddEditMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_EDIT_SCHEMA_METADATA, AddEditSchemaMetadataViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(CONFIG_MANAGEMENT, ConfigManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LDAP_CONFIG_MANAGEMENT, LDAPConfigManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_DISPLAY_FORM, DisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(SEARCH_DISPLAY_FORM, SearchDisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(FORM_DISPLAY_FORM, FormDisplayConfigViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_PRINCIPAL_AUTHORIZATIONS, ListPrincipalAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_OBJECT_AUTHORIZATIONS, ListContentAuthorizationsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_LIST, ListCollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER, CollectionUserViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_USER_ROLES, CollectionUserRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP, CollectionGroupViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_GROUP_ROLES, CollectionGroupRolesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_VALUE_DOMAINS, ListValueDomainViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_TAXONOMY, ListTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(TAXONOMY_ADD_EDIT, AddEditTaxonomyViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(CONTAINERS_BY_ADMIN_UNITS, ContainersByAdministrativeUnitsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS,
				ContainersInAdministrativeUnitViewImpl.class));
		viewProviders
				.add(new ClassBasedViewProvider(DISPLAY_FILING_SPACE_WITH_CONTAINERS, ContainersInFilingSpaceViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_CONTAINER, DisplayContainerViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(PERMISSION_MANAGEMENT, PermissionsManagementViewImpl.class));

		// TODO Use generic system to configure
		viewProviders.add(new ClassBasedViewProvider(ADMIN_MODULE, AdminRMModuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENTS_LIST, EventCategoriesViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_CATEGORY, BaseEventCategoryViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EVENT_DISPLAY, EventViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(REPORTS, RMReportsViewImpl.class));
		//		viewProviders.add(new ClassBasedViewProvider(REPORTS, RMReportsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ARCHIVES_MANAGEMENT, ArchiveManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING, DecommissioningMainViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_BUILDER, DecommissioningBuilderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_DISPLAY, DecommissioningListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_EDIT, EditDecommissioningListViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DECOMMISSIONING_LIST_ADD_NEW_CONTAINER, AddNewContainerViewImpl.class));
		viewProviders.add(
				new ClassBasedViewProvider(DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER, AddExistingContainerViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_METADATA_GROUP, ListMetadataGroupSchemaTypeViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(LIST_ONGLET, ListMetadataGroupSchemaTypeViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(RECORDS_MANAGEMENT, RecordsManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_FOLDER, AddEditFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_FOLDER, AddEditFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_FOLDER, DisplayFolderViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_DOCUMENT, AddEditDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_DOCUMENT, AddEditDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_DOCUMENT, DisplayDocumentViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(ADD_RETENTION_RULE, AddEditRetentionRuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(EDIT_RETENTION_RULE, AddEditRetentionRuleViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(DISPLAY_RETENTION_RULE, DisplayRetentionRuleViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_RETENTION_RULES, ListRetentionRulesViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(LIST_USER_DOCUMENTS, ListUserDocumentsViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_MANAGEMENT, CollectionManagementViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(COLLECTION_ADD_EDIT, AddEditCollectionViewImpl.class));
		viewProviders.add(new ClassBasedViewProvider(MODIFY_PROFILE, ModifyProfileViewImpl.class));

		viewProviders.add(new ClassBasedViewProvider(UPDATE_MANAGER, UpdateManagerViewImpl.class));
	}

	public void configure(Navigator navigator) {
		for (ViewProvider viewProvider : viewProviders) {
			navigator.addProvider(viewProvider);
		}
	}
}
