package com.constellio.app.ui.pages.security;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainPresenter;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.ADD_RETENTION_RULE;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.ARCHIVES_MANAGEMENT;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DECOMMISSIONING;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DECOMMISSIONING_LIST_ADD_NEW_CONTAINER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DECOMMISSIONING_LIST_BUILDER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DECOMMISSIONING_LIST_EDIT;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_ADMIN_UNIT_WITH_CONTAINERS;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_CONTAINER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_DOCUMENT;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_FILING_SPACE_WITH_CONTAINERS;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_FOLDER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.DISPLAY_RETENTION_RULE;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.EDIT_DOCUMENT;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.EDIT_FOLDER;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.EDIT_RETENTION_RULE;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.LIST_RETENTION_RULES;
import static com.constellio.app.modules.rm.navigation.RMNavigationConfiguration.REPORTS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.ADD_EDIT_METADATA;
import static com.constellio.app.ui.application.NavigatorConfigurationService.ADD_EDIT_SCHEMA;
import static com.constellio.app.ui.application.NavigatorConfigurationService.ADD_EDIT_SCHEMA_METADATA;
import static com.constellio.app.ui.application.NavigatorConfigurationService.ADD_EDIT_SCHEMA_RECORD;
import static com.constellio.app.ui.application.NavigatorConfigurationService.ADMIN_MODULE;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_ADD_EDIT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_GROUP;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_GROUP_ROLES;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_USER;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_USER_LIST;
import static com.constellio.app.ui.application.NavigatorConfigurationService.COLLECTION_USER_ROLES;
import static com.constellio.app.ui.application.NavigatorConfigurationService.CONFIG_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.DISPLAY_SCHEMA_RECORD;
import static com.constellio.app.ui.application.NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE;
import static com.constellio.app.ui.application.NavigatorConfigurationService.EDIT_DISPLAY_FORM;
import static com.constellio.app.ui.application.NavigatorConfigurationService.EVENTS_LIST;
import static com.constellio.app.ui.application.NavigatorConfigurationService.EVENT_CATEGORY;
import static com.constellio.app.ui.application.NavigatorConfigurationService.EVENT_DISPLAY;
import static com.constellio.app.ui.application.NavigatorConfigurationService.EXPORTER;
import static com.constellio.app.ui.application.NavigatorConfigurationService.FORM_DISPLAY_FORM;
import static com.constellio.app.ui.application.NavigatorConfigurationService.GROUP_ADD_EDIT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.GROUP_DISPLAY;
import static com.constellio.app.ui.application.NavigatorConfigurationService.GROUP_LIST;
import static com.constellio.app.ui.application.NavigatorConfigurationService.IMPORT_FILE;
import static com.constellio.app.ui.application.NavigatorConfigurationService.IMPORT_SCHEMA_TYPES;
import static com.constellio.app.ui.application.NavigatorConfigurationService.IMPORT_USERS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LDAP_CONFIG_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_ONGLET;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_SCHEMA_RECORDS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_TAXONOMY;
import static com.constellio.app.ui.application.NavigatorConfigurationService.LIST_VALUE_DOMAINS;
import static com.constellio.app.ui.application.NavigatorConfigurationService.PERMISSION_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.RECORDS_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.SEARCH_DISPLAY_FORM;
import static com.constellio.app.ui.application.NavigatorConfigurationService.TAXONOMY_ADD_EDIT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.TAXONOMY_CONCEPT_ADD_EDIT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.TAXONOMY_MANAGEMENT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.UPDATE_MANAGER;
import static com.constellio.app.ui.application.NavigatorConfigurationService.USER_ADD_EDIT;
import static com.constellio.app.ui.application.NavigatorConfigurationService.USER_DISPLAY;
import static com.constellio.app.ui.application.NavigatorConfigurationService.USER_LIST;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class UserSecurityAcceptanceTest extends ConstellioTest {
	// TODO: All of these constants are duplicated from somewhere else and must be removed

	public static final String SYSTEM_USAGE = "OPEN_SESSION";
	public static final String USERS_AND_GROUPS_ADD_OR_REMOVE = "USERS_AND_GROUPS_ADD_OR_REMOVE";
	public static final String FOLDERS_AND_DOCUMENTS_CREATION = "FOLDERS_AND_DOCUMENTS_CREATION";
	public static final String FOLDERS_AND_DOCUMENTS_MODIFICATION = "FOLDERS_AND_DOCUMENTS_MODIFICATION";
	public static final String FOLDERS_AND_DOCUMENTS_DELETION = "FOLDERS_AND_DOCUMENTS_DELETION";
	public static final String CURRENTLY_BORROWED_DOCUMENTS = "CURRENTLY_BORROWED_DOCUMENTS";
	public static final String DOCUMENTS_BORROW_OR_RETURN = "DOCUMENTS_BORROW_OR_RETURN";
	public static final String EVENTS_BY_ADMINISTRATIVE_UNIT = "EVENTS_BY_ADMINISTRATIVE_UNIT";
	public static final String EVENTS_BY_FOLDER = "EVENTS_BY_FOLDER";
	public static final String EVENTS_BY_USER = "EVENTS_BY_USER";
	public static final String DECOMMISSIONING_EVENTS = "DECOMMISSIONING_EVENTS";

	public static final String RECORDS_MANAGEMENT_LINK = "recordsManagement";
	public static final String USER_DOCUMENTS_LINK = "userDocuments";
	public static final String ARCHIVES_MANAGEMENT_LINK = "archivesManagemen";
	public static final String LOGS_LINK = "logs";
	public static final String ADMIN_MODULE_LINK = "adminModule";
	public static final String SYSTEM_USAGE_LINK = "systemUsageLinkButton";
	public static final String USERS_AND_GROUPS_LINK = "usersAndGroupsLinkButton";
	public static final String RECORDS_CREATION_LINK = "recordsCreationLinkButton";
	public static final String RECORDS_MODIFICATION_LINK = "recordsModificationLinkButton";
	public static final String RECORDS_DELETION_LINK = "recordsDeletionLinkButton";
	public static final String CURRENTLY_BORROWED_DOCUMENTS_LINK = "currentlyBorrowedDocumentsLinkButton";
	public static final String BORROWED_DOCUMENTS_LINK = "borrowedDocumentsLinkButton";
	public static final String FILING_SPACE_EVENTS_LINK = "filingSpaceEventsLinkButton";
	public static final String BY_FOLDER_EVENTS_LINK = "byFolderEventsLinkButton";
	public static final String BY_USER_EVENTS_LINK = "byUserEventsLinkButton";
	public static final String DECOMMISSIONING_EVENTS_LINK = "decommissioningEventsLinkButton";

	public static final String DECOMMISSIONING_CAPTION = "decommissioning-caption";
	public static final String REPORTS_CAPTION = "reports-caption";
	public static final String FACETS_CAPTION = "facets-caption";
	public static final String CONTAINERS_CAPTION = "containers-caption";
	public static final String ROBOTS_CAPTION = "robots-caption";

	String taskIdThatEdouardCanModifyAndDakotaCanView;

	RolesManager rolesManager;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	String documentInA13;
	String documentInC04;
	String semiactiveFolderA49;
	String inactiveFolderA81;
	String documentInA49;
	String documentInA81;

	String edouardIdInZeCollection, dakotaInZeCollection, sasquatchInZeCollection;
	String legendsInZeCollection;

	String customTaxonomyCode;
	UserServices userServices;
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		semiactiveFolderA49 = records.folder_A49;
		inactiveFolderA81 = records.folder_A81;

		documentInA13 = recordIdWithTitleInCollection("Cerf - Typologie", zeCollection);
		documentInC04 = recordIdWithTitleInCollection("Céleri - Typologie", zeCollection);
		documentInA49 = recordIdWithTitleInCollection("Grenouille - Livre de recettes", zeCollection);
		documentInA81 = recordIdWithTitleInCollection("Moineau - Livre de recettes", zeCollection);
		rolesManager = getModelLayerFactory().getRolesManager();

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "Ze taxo!");

		customTaxonomyCode = new ValueListServices(getAppLayerFactory(), zeCollection).createTaxonomy(labelTitle1, true).getCode();

		rolesManager.addRole(new Role(zeCollection, "roleDeAlice", "Role de Alice", new ArrayList<String>()));

		userServices = getModelLayerFactory().newUserServices();

		edouardIdInZeCollection = userServices.getUserInCollection(edouard, zeCollection).getId();
		dakotaInZeCollection = userServices.getUserInCollection(dakota, zeCollection).getId();
		sasquatchInZeCollection = userServices.getUserInCollection(sasquatch, zeCollection).getId();
		legendsInZeCollection = userServices.getGroupInCollection("legends", zeCollection).getId();

		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Task zeTask = schemas.newTask();
		zeTask.setTitle("ze task");
		zeTask.setCreatedBy(edouardIdInZeCollection);
		zeTask.setAssignee(edouardIdInZeCollection);
		zeTask.setAssignationDate(new LocalDate());
		zeTask.setAssigner(edouardIdInZeCollection);
		zeTask.setTaskFollowers(asList(
				new TaskFollower().setFollowerId(dakotaInZeCollection)
		));
		recordServices.add(zeTask);
		taskIdThatEdouardCanModifyAndDakotaCanView = zeTask.getId();
	}

	@Test
	public void givenAdminInZeCollectionThenCanOnlyNavigateToPagesAccordingToHimPermissions() {

		logAsIn(admin, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//DOC
		assertThat(navigationToFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateDocumentPossible(records.folder_A13)).isTrue();
		assertThat(navigationToEditFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_A13)).isTrue();
		assertThat(navigationToShareContentPossible(records.folder_A13)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isTrue();
		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA13)).isTrue();

		assertThatCanNavigateToAllArchivesPages();
		assertThatCanNavigateToAllEventPages();
		assertThatCanNavigateToAllAdminModulePages();
	}

	@Test
	public void givenDakotaInZeCollectionThenCanOnlyNavigateToPagesAccordingToHerPermissions() {

		logAsIn(dakota, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//Active
		assertThatCanNavigateToAllActiveRMPages();

		//DOC NOT OK
		assertThat(navigationToFolderPossible(records.folder_C04)).isFalse();
		assertThat(navigationToCreateSubFolderPossible(records.folder_C04)).isFalse();
		assertThat(navigationToCreateDocumentPossible(records.folder_C04)).isFalse();
		assertThat(navigationToEditFolderPossible(records.folder_C04)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_C04)).isFalse();
		assertThat(navigationToShareContentPossible(records.folder_C04)).isFalse();

		assertThat(navigationToDocumentPossible(documentInC04)).isFalse();
		assertThat(navigationToEditDocumentPossible(documentInC04)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInC04)).isFalse();
		assertThat(navigationToShareContentPossible(documentInC04)).isFalse();
	}

	@Test
	public void givenAliceHasWriteAccessAndUserRoleInZeCollectionThenCanOnlyNavigateToPagesAccordingToHerPermissions()
			throws RecordServicesException {

		UserServices userServices = getModelLayerFactory().newUserServices();
		User alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		recordServices.update(alice.setCollectionWriteAccess(true).setUserRoles(asList(RMRoles.USER)));

		logAsIn(aliceWonderland, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//Active
		assertThat(navigationToFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateDocumentPossible(records.folder_A13)).isTrue();
		assertThat(navigationToEditFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_A13)).isFalse();
		assertThat(navigationToShareContentPossible(records.folder_A13)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isFalse();
		assertThat(navigationToShareContentPossible(documentInA13)).isTrue();

		//Semiactive
		assertThat(navigationToFolderPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToCreateDocumentPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToEditFolderPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToShareContentPossible(semiactiveFolderA49)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA49)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA49)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA49)).isFalse();
		assertThat(navigationToShareContentPossible(documentInA49)).isTrue();

		//Inactive
		assertThat(navigationToFolderPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToCreateDocumentPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToEditFolderPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToShareContentPossible(inactiveFolderA81)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA81)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA81)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA81)).isFalse();
		assertThat(navigationToShareContentPossible(documentInA81)).isTrue();
	}

	@Test
	public void givenAliceHasWriteAccessAndManagerRoleInZeCollectionThenCanOnlyNavigateToPagesAccordingToHerPermissions()
			throws RecordServicesException {

		UserServices userServices = getModelLayerFactory().newUserServices();
		User alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		recordServices.update(alice.setCollectionWriteAccess(true).setUserRoles(asList(RMRoles.MANAGER)));

		logAsIn(aliceWonderland, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//Active
		assertThatCanNavigateToAllActiveRMPages();

		//Semiactive
		assertThat(navigationToFolderPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToCreateDocumentPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToEditFolderPossible(semiactiveFolderA49)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToShareContentPossible(semiactiveFolderA49)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA49)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA49)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA49)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA49)).isTrue();

		//Inactive
		assertThat(navigationToFolderPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToCreateDocumentPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToEditFolderPossible(inactiveFolderA81)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToShareContentPossible(inactiveFolderA81)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA81)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA81)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA81)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA81)).isTrue();
	}

	@Test
	public void givenAliceHasWriteAccessAndRGDRoleInZeCollectionThenCanOnlyNavigateToPagesAccordingToHerPermissions()
			throws RecordServicesException {

		UserServices userServices = getModelLayerFactory().newUserServices();
		User alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		recordServices.update(alice.setCollectionWriteAccess(true).setUserRoles(asList(RMRoles.RGD)));

		logAsIn(aliceWonderland, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//Active
		assertThatCanNavigateToAllActiveRMPages();

		//Semiactive
		assertThat(navigationToFolderPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToCreateDocumentPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToEditFolderPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(semiactiveFolderA49)).isTrue();
		assertThat(navigationToShareContentPossible(semiactiveFolderA49)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA49)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA49)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA49)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA49)).isTrue();

		//Inactive
		assertThat(navigationToFolderPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToCreateDocumentPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToEditFolderPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(inactiveFolderA81)).isTrue();
		assertThat(navigationToShareContentPossible(inactiveFolderA81)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA81)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA81)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA81)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA81)).isTrue();
	}

	@Test
	public void givenAliceInZeCollectionThenCanOnlyNavigateToPagesAccordingToHerPermissions() {

		logAsIn(aliceWonderland, zeCollection);

		//Keep or not to keep?
		//		assertThat(navigationToCreateFolderPossible()).isFalse();
		//		assertThat(navigationToAddDocumentPossible()).isFalse();

		//DOC
		assertThat(navigationToFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(records.folder_A13)).isFalse();
		assertThat(navigationToCreateDocumentPossible(records.folder_A13)).isFalse();
		assertThat(navigationToEditFolderPossible(records.folder_A13)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_A13)).isFalse();
		assertThat(navigationToShareContentPossible(records.folder_A13)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isFalse();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isFalse();
		assertThat(navigationToShareContentPossible(documentInA13)).isTrue();

		assertThatCannotNavigateToAllArchivesPages();
		assertThatCannotNavigateToAllEventPages();
		assertThatCannotNavigateToAllAdminModulePages();
	}

	@Test
	public void whenUsingSchemaPageThenCanOnlyModifyAllowedSchemaTypes()
			throws Exception {
		logAsIn(admin, zeCollection);

		assertThat(navigateToSchemaRecordList(DocumentType.DEFAULT_SCHEMA)).isTrue();
		assertThat(navigateToSchemaRecordDisplay(records.documentTypeId_3)).isTrue();
		assertThat(navigateToSchemaRecordAdd(DocumentType.DEFAULT_SCHEMA)).isTrue();
		assertThat(navigateToSchemaRecordEdit(DocumentType.DEFAULT_SCHEMA, records.documentTypeId_3)).isTrue();

		assertThat(navigateToSchemaRecordList(StorageSpace.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.storageSpaceId_S01)).isFalse();
		assertThat(navigateToSchemaRecordAdd(StorageSpace.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(StorageSpace.DEFAULT_SCHEMA, records.storageSpaceId_S01)).isFalse();

		assertThat(navigateToSchemaRecordList(Category.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.categoryId_X120)).isFalse();
		assertThat(navigateToSchemaRecordAdd(Category.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(Category.DEFAULT_SCHEMA, records.categoryId_X120)).isFalse();

		assertThat(navigateToSchemaRecordList(AdministrativeUnit.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.unitId_11)).isFalse();
		assertThat(navigateToSchemaRecordAdd(AdministrativeUnit.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(AdministrativeUnit.DEFAULT_SCHEMA, records.unitId_11)).isFalse();

		assertThat(navigateToSchemaRecordList(RetentionRule.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.ruleId_2)).isFalse();
		assertThat(navigateToSchemaRecordAdd(RetentionRule.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(RetentionRule.DEFAULT_SCHEMA, records.ruleId_2)).isFalse();

		assertThat(navigateToSchemaRecordList(Folder.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.folder_A10)).isFalse();
		assertThat(navigateToSchemaRecordAdd(Folder.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(Folder.DEFAULT_SCHEMA, records.folder_A10)).isFalse();

		assertThat(navigateToSchemaRecordList(User.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.getBob_userInAC().getId())).isFalse();
		assertThat(navigateToSchemaRecordAdd(User.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(User.DEFAULT_SCHEMA, records.getBob_userInAC().getId())).isFalse();

		assertThat(navigateToSchemaRecordList(Group.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.getLegends().getId())).isFalse();
		assertThat(navigateToSchemaRecordAdd(Group.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(Group.DEFAULT_SCHEMA, records.getLegends().getId())).isFalse();

		assertThat(navigateToSchemaRecordList(Event.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordAdd(Event.DEFAULT_SCHEMA)).isFalse();

		assertThat(navigateToSchemaRecordList(Collection.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(zeCollection)).isFalse();
		assertThat(navigateToSchemaRecordAdd(Collection.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(Collection.DEFAULT_SCHEMA, zeCollection)).isFalse();

		assertThat(navigateToSchemaRecordList(UniformSubdivision.DEFAULT_SCHEMA)).isTrue();
		assertThat(navigateToSchemaRecordDisplay(records.subdivId_2)).isTrue();
		assertThat(navigateToSchemaRecordAdd(UniformSubdivision.DEFAULT_SCHEMA)).isTrue();
		assertThat(navigateToSchemaRecordEdit(UniformSubdivision.DEFAULT_SCHEMA, records.subdivId_2)).isTrue();

		assertThat(navigateToSchemaRecordList(DecommissioningList.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.list_02)).isFalse();
		assertThat(navigateToSchemaRecordAdd(DecommissioningList.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(DecommissioningList.DEFAULT_SCHEMA, records.list_02)).isFalse();

		assertThat(navigateToSchemaRecordList(ContainerRecord.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(records.containerId_bac07)).isFalse();
		assertThat(navigateToSchemaRecordAdd(ContainerRecord.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(ContainerRecord.DEFAULT_SCHEMA, records.containerId_bac07)).isFalse();

		assertThat(navigateToSchemaRecordList(Document.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordDisplay(documentInA13)).isFalse();
		assertThat(navigateToSchemaRecordAdd(Document.DEFAULT_SCHEMA)).isFalse();
		assertThat(navigateToSchemaRecordEdit(Document.DEFAULT_SCHEMA, documentInA13)).isFalse();
	}

	@Test
	public void givenAliceInZeCollectionThenCanDoEverythingGivenSheHaveRequiredPermissions()
			throws Exception {
		logAsIn(aliceWonderland, zeCollection);

		UserServices userServices = getModelLayerFactory().newUserServices();
		recordServices.update(userServices.getUserRecordInCollection(aliceWonderland, zeCollection)
				.setUserRoles(asList("roleDeAlice")));

		givenAliceHasPermission(RMPermissionsTo.CREATE_FOLDERS);
		assertThatUserInGlobalMenuHasOnlyAccessTo(RECORDS_MANAGEMENT_LINK, USER_DOCUMENTS_LINK);
		assertThatWhenNavigatingToHomePageUserHasOnlyAccessTo(
				RMNavigationConfiguration.ADD_FOLDER, RMNavigationConfiguration.ADD_DOCUMENT);
		assertThat(navigationToCreateFolderPossible()).isTrue();

		givenAliceHasPermission(RMPermissionsTo.CREATE_DOCUMENTS);
		assertThat(navigationToAddDocumentPossible()).isTrue();

		givenAliceHasPermission(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
		assertThatUserHasOnlyAccessToRMArchivesPages(DECOMMISSIONING_CAPTION);
		assertThatUserHasAccessToAdminPages();
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllDecommissioningPages();

		givenAliceHasPermission(RMPermissionsTo.MANAGE_CONTAINERS);
		assertThatUserHasOnlyAccessToRMArchivesPages(CONTAINERS_CAPTION);
		assertThatUserHasAccessToAdminPages();
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllContainersPages();

		givenAliceHasPermission(RMPermissionsTo.MANAGE_REPORTS);
		assertThatUserHasOnlyAccessToRMArchivesPages(REPORTS_CAPTION);
		assertThatUserHasAccessToAdminPages();
		assertThatUserHasAccessTORMEventsPages();
		assertThat(navigateToReportsPossible()).isTrue();

		givenAliceHasPermission(CorePermissions.VIEW_EVENTS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages();
		assertThatUserHasAccessTORMEventsPages(SYSTEM_USAGE_LINK, USERS_AND_GROUPS_LINK, RECORDS_CREATION_LINK,
				RECORDS_MODIFICATION_LINK, RECORDS_DELETION_LINK, CURRENTLY_BORROWED_DOCUMENTS_LINK, BORROWED_DOCUMENTS_LINK,
				FILING_SPACE_EVENTS_LINK, BY_FOLDER_EVENTS_LINK, BY_USER_EVENTS_LINK, DECOMMISSIONING_EVENTS_LINK);
		assertThatCanNavigateToAllEventPages();

		givenAliceHasPermission(CorePermissions.MANAGE_TAXONOMIES);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.TAXONOMIES);
		assertThatCanNavigateToTaxonomiesPages();
		assertThatCannotNavigateToAllClassificationPlanPages();

		givenAliceHasPermission(CorePermissions.MANAGE_FACETS);
		assertThatUserHasAccessToAdminPages(FACETS_CAPTION);
		assertThat(navigateToAddFacetPossible()).isTrue();
		assertThat(navigateToEditFacetPossible()).isTrue();
		assertThat(navigateToDisplayFacetPossible()).isTrue();
		assertThat(navigateToListFacetPossible()).isTrue();
		assertThat(navigateToOrderFacetPossible()).isTrue();

		givenAliceHasPermission(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.TAXONOMIES);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllClassificationPlanPages();

		givenAliceHasPermission(CorePermissions.MANAGE_SECURITY);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.SECURITY, CoreNavigationConfiguration.TAXONOMIES,
				CoreNavigationConfiguration.ROLES);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllSecurityPages();

		givenAliceHasPermission(RMPermissionsTo.MANAGE_UNIFORMSUBDIVISIONS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(RMNavigationConfiguration.UNIFORM_SUBDIVISIONS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllUniformSubdivisionsPages();

		givenAliceHasPermission(CorePermissions.MANAGE_VALUELIST);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.VALUE_DOMAINS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllValueDomainPages();

		givenAliceHasPermission(CorePermissions.MANAGE_METADATASCHEMAS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.METADATA_SCHEMAS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllMetadataSchemaPages();

		givenAliceHasPermission(RMPermissionsTo.MANAGE_RETENTIONRULE);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(RMNavigationConfiguration.RETENTION_CALENDAR);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllRetentionRulesPages();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_CONFIGURATION);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.CONFIG);
		assertThatUserHasAccessTORMEventsPages();
		assertThat(navigateToConfigurationPossible()).isTrue();

		givenAliceHasPermission(CorePermissions.MANAGE_LDAP);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.LDAP_CONFIG);
		assertThatUserHasAccessTORMEventsPages();
		assertThat(navigateToLdapPossible()).isTrue();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_GROUPS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.GROUPS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllSystemGroupPages();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_USERS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.USERS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllSystemUsersPages();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_COLLECTIONS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.COLLECTIONS);
		assertThatUserHasAccessTORMEventsPages();
		assertThatCanNavigateToAllSystemCollectionsPages();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_UPDATES);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.UPDATE_CENTER);
		assertThatUserHasAccessTORMEventsPages();
		assertThat(navigateToUpdateCenterPossible()).isTrue();

		givenAliceHasPermission(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS);
		assertThatUserHasOnlyAccessToRMArchivesPages();
		assertThatUserHasAccessToAdminPages(CoreNavigationConfiguration.IMPORT_RECORDS);
		assertThatUserHasAccessTORMEventsPages();
		assertThat(navigateToImportRecordsPossible()).isTrue();
		assertThat(navigateToImportSchemaTypesPossible()).isTrue();
		assertThat(navigateToImportUsersPossible()).isTrue();
		assertThat(navigateToExportPossible()).isTrue();
	}

	@Test
	public void givenSasquatchCanReadEverythingInACollectionAndOnlySomeFoldersInAnOtherThenSecure()
			throws Exception {

		rolesManager.addRole(new Role(zeCollection, "roleDeSasquatch", "Role de Alice",
				asList(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN)));
		rolesManager.addRole(new Role("LaCollectionDeRida", "roleDeSasquatch", "Role de Alice",
				asList(RMPermissionsTo.MANAGE_RETENTIONRULE)));

		User sasquatchInZeCollection = userServices.getUserInCollection(sasquatch, zeCollection);
		User sasquatchInOtherCollection = userServices.getUserInCollection(sasquatch, "LaCollectionDeRida");
		recordServices.update(sasquatchInZeCollection.setCollectionReadAccess(true).setUserRoles(asList("roleDeSasquatch")));
		recordServices.update(sasquatchInOtherCollection.setUserRoles(asList("roleDeSasquatch")));
		giveAReadAccessToAFolderToSasquatch(sasquatchInOtherCollection, records2.folder_A01);
		waitForBatchProcess();

		//Starts on LaCollectionDeRida
		logAsIn(sasquatch, "LaCollectionDeRida");
		assertThat(navigationToFolderPossible(records2.folder_A01)).isTrue();
		assertThat(navigateToCategoriesPlanPossible()).isFalse();
		assertThat(navigateToListRetentionRulesPossible()).isTrue();

		//Switching to zeCollection
		assertThat(navigationToFolderPossible(records.folder_C01)).isTrue();
		assertThat(navigateToCategoriesPlanPossible()).isTrue();
		assertThat(navigateToListRetentionRulesPossible()).isFalse();

		//Has no access to that record in LaCollectionDeRida, still in zeCollection
		assertThat(navigationToFolderPossible(records2.folder_A03)).isFalse();
		assertThat(navigateToCategoriesPlanPossible()).isTrue();
		assertThat(navigateToListRetentionRulesPossible()).isFalse();

		//Has access to that record in LaCollectionDeRida, switching...
		assertThat(navigationToFolderPossible(records2.folder_A01)).isTrue();
		assertThat(navigateToCategoriesPlanPossible()).isFalse();
		assertThat(navigateToListRetentionRulesPossible()).isTrue();

		//Removing user from zeCollection
		userServices.removeUserFromCollection(sasquatch, "zeCollection");
		assertThat(navigateToCategoriesPlanPossible()).isFalse();
		assertThat(navigateToListRetentionRulesPossible()).isTrue();

		//Switching to zeCollection failed, still in LaCollectionDeRida
		assertThat(navigationToFolderPossible(records.folder_C01)).isFalse();
		assertThat(navigateToCategoriesPlanPossible()).isFalse();

		//waitUntilICloseTheBrowsers();
		assertThat(navigationToFolderPossible(records2.folder_A01)).isTrue();
		//waitUntilICloseTheBrowsers();
		assertThat(navigateToListRetentionRulesPossible()).isTrue();

	}

	private void giveAReadAccessToAFolderToSasquatch(User user, String recordId) {
		getModelLayerFactory().newAuthorizationsServices().add(authorizationForUsers(user).on(recordId).givingReadAccess());
	}

	@Test
	public void givenGandalfInZeCollectionThenCanOnlyNavigateToPagesAccordingToHimPermissions() {

		logAsIn(gandalf, zeCollection);

		assertThat(navigationToCreateFolderPossible()).isTrue();
		assertThat(navigationToAddDocumentPossible()).isTrue();

		//DOC
		assertThat(navigationToFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateDocumentPossible(records.folder_A13)).isTrue();
		assertThat(navigationToEditFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_A13)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isTrue();

		//assertThatCanNavigateToAllDecommissioningPages();
		assertThatCanNavigateToAllContainersPages();

		assertThat(navigateToReportsPossible()).isFalse();

		assertThatCannotNavigateToAllEventPages();
		assertThatCannotNavigateToAllAdminModulePages();
	}

	@Test
	public void givenDakotaCannotNavigateToManageSecurityPages() {

		logAsIn(dakota, zeCollection);

		assertThatCannotNavigateToAllAdminModulePages();
	}

	@Test
	public void givenAliceWithCollectionReadAccessThenCanHasValidTaskAccess() {

		logAsIn(aliceWonderland, zeCollection);

		assertThat(navigationToTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isTrue();
		assertThat(navigationToEditTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isFalse();
	}

	@Test
	public void givenSasquatchThenCanHasValidTaskAccess() {

		logAsIn(sasquatch, zeCollection);

		assertThat(navigationToTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isFalse();
		assertThat(navigationToEditTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isFalse();
	}

	@Test
	public void givenDakotaThenCanHasValidTaskAccess() {

		logAsIn(dakota, zeCollection);

		assertThat(navigationToTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isTrue();
		assertThat(navigationToEditTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isFalse();
	}

	@Test
	public void givenEdouardThenCanHasValidTaskAccess() {

		logAsIn(edouard, zeCollection);

		assertThat(navigationToTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isTrue();
		assertThat(navigationToEditTaskPossible(taskIdThatEdouardCanModifyAndDakotaCanView)).isTrue();
	}

	// ----------------------------------------------------------------------------------

	@Test
	@InDevelopmentTest
	public void givenDecommissionningListWithValidationRequestByDakotaThenDakotaCanAccessPageAndTab() {

		logAsIn(dakota, zeCollection);

		assertThat(navigateToDecommissioningListPossible(records.list_24)).isTrue();
		List<String> allTabs = Arrays.asList(DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PROCESSED, DecommissioningMainPresenter.PENDING_VALIDATION,
				DecommissioningMainPresenter.TO_VALIDATE);

		checkExpectedTabs(allTabs, DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PROCESSED, DecommissioningMainPresenter.PENDING_VALIDATION,
				DecommissioningMainPresenter.TO_VALIDATE);
	}

	@Test
	public void givenDecommissionningListWithValidationRequestByDakotaAndBobInValidationThenBobCanAccessPageAndTab() {

		logAsIn(bobGratton, zeCollection);

		assertThat(navigateToDecommissioningListPossible(records.list_24)).isTrue();
		List<String> allTabs = Arrays.asList(DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PROCESSED, DecommissioningMainPresenter.PENDING_VALIDATION,
				DecommissioningMainPresenter.TO_VALIDATE);

		checkExpectedTabs(allTabs, DecommissioningMainPresenter.TO_VALIDATE);
	}

	@Test
	public void givenDecommissionningListWithValidationRequestByDakotaThenGandalfCanAccessPageAndNotTab() {

		logAsIn(bobGratton, zeCollection);

		assertThat(navigateToDecommissioningListPossible(records.list_24)).isTrue();
		List<String> allTabs = Arrays.asList(DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PROCESSED, DecommissioningMainPresenter.PENDING_VALIDATION,
				DecommissioningMainPresenter.TO_VALIDATE);

		checkExpectedTabs(allTabs, DecommissioningMainPresenter.TO_VALIDATE);
	}

	@Test
	public void givenDecommissionningListWithValidationRequestByDakotaThenAliceCannotAccessPage() {

		logAsIn(aliceWonderland, zeCollection);

		assertThat(navigateToDecommissioningListPossible(records.list_24)).isFalse();
	}

	private void checkExpectedTabs(List<String> allTabs, String... tabExpected) {
		for (String aTab : allTabs) {
			if (tabIsExpected(aTab, tabExpected)) {
				assertThat(tabByClassIsVisible(aTab)).describedAs("Tab " + aTab + " visibility").isTrue();
			} else {
				assertThat(tabByClassIsVisible(aTab)).describedAs("Tab " + aTab + " visibility").isFalse();
			}
		}
	}

	private boolean tabIsExpected(String aTab, String... tabExpected) {
		for (String aTabExpected : tabExpected) {
			if (aTabExpected.equals(aTab)) {
				return true;
			}
		}
		return false;
	}

	private Boolean tabByClassIsVisible(String aTabClass) {
		//TODO find a way to find the tabs after the first
		try {
			driver.findElement(By.id(aTabClass));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// -----------------------------------------------------------------------------------

	private void assertThatWhenNavigatingToHomePageUserHasOnlyAccessTo(String... buttonsExpected) {
		driver.navigateTo().url(RECORDS_MANAGEMENT);
		List<String> allButtons = Arrays.asList(RMNavigationConfiguration.ADD_FOLDER, RMNavigationConfiguration.ADD_DOCUMENT);
		checkExpectedButtons(allButtons, buttonsExpected);
	}

	private void assertThatUserHasNoAccessToRMArchivesModule() {
		assertThat(navigateToArchivesManagementPossible()).isFalse();
	}

	private void assertThatUserHasOnlyAccessToRMArchivesPages(String... buttonsExpected) {
		driver.logUserInCollection(aliceWonderland, zeCollection);
		if (buttonsExpected.length == 0) {
			assertThatUserHasNoAccessToRMArchivesModule();
			return;
		}

		//TODO : link not visible
		//assertThat(buttonByClassIsVisible("archivesManagementLink")).isTrue();

		driver.navigateTo().url(ARCHIVES_MANAGEMENT);

		List<String> allButtons = new ArrayList<String>();
		allButtons.add(DECOMMISSIONING_CAPTION);
		allButtons.add(CONTAINERS_CAPTION);
		allButtons.add(ROBOTS_CAPTION);
		allButtons.add(REPORTS_CAPTION);

		checkExpectedButtons(allButtons, buttonsExpected);
	}

	private void assertThatUserHasNoAccessToRMAdminModule() {
		//assertThat(buttonByClassIsVisible("adminModuleLink")).isFalse();
		assertThat(navigateToAdminModulePossible()).isFalse();
	}

	private void assertThatUserHasAccessToAdminPages(String... buttonsExpected) {
		driver.logUserInCollection(aliceWonderland, zeCollection);
		if (buttonsExpected.length == 0) {
			assertThatUserHasNoAccessToRMAdminModule();
			return;
		}

		//TODO link not visible
		//assertThat(buttonByClassIsVisible("adminModuleLink")).isTrue();

		driver.navigateTo().url(ADMIN_MODULE);

		List<String> allButtons = new ArrayList<>();
		allButtons.add(CoreNavigationConfiguration.TAXONOMIES);
		allButtons.add(CoreNavigationConfiguration.VALUE_DOMAINS);
		allButtons.add(RMNavigationConfiguration.RETENTION_CALENDAR);
		allButtons.add(CoreNavigationConfiguration.METADATA_SCHEMAS);
		allButtons.add(RMNavigationConfiguration.UNIFORM_SUBDIVISIONS);
		allButtons.add(CoreNavigationConfiguration.ROLES);
		allButtons.add(CoreNavigationConfiguration.SECURITY);
		allButtons.add(CoreNavigationConfiguration.UPDATE_CENTER);
		allButtons.add(CoreNavigationConfiguration.COLLECTIONS);
		allButtons.add(CoreNavigationConfiguration.USERS);
		allButtons.add(CoreNavigationConfiguration.GROUPS);
		allButtons.add(CoreNavigationConfiguration.IMPORT_RECORDS);
		allButtons.add(CoreNavigationConfiguration.LDAP_CONFIG);
		allButtons.add(CoreNavigationConfiguration.CONFIG);

		checkExpectedButtons(allButtons, buttonsExpected);
	}

	private void assertThatUserHasAccessTORMEventsPages(String... buttonsExpected) {
		//driver.logUserInCollection(aliceWonderland, zeCollection);
		if (buttonsExpected.length == 0) {
			assertThatUserHasNoAccessToRMEvents();
			return;
		}

		//TODO : link not visible
		//assertThat(buttonByClassIsVisible("logsLink")).isTrue();

		driver.navigateTo().url(EVENTS_LIST);

		List<String> allButtons = new ArrayList<String>();
		allButtons.add(SYSTEM_USAGE_LINK);
		allButtons.add(USERS_AND_GROUPS_LINK);
		allButtons.add(RECORDS_CREATION_LINK);
		allButtons.add(RECORDS_MODIFICATION_LINK);
		allButtons.add(RECORDS_DELETION_LINK);
		allButtons.add(CURRENTLY_BORROWED_DOCUMENTS_LINK);
		allButtons.add(BORROWED_DOCUMENTS_LINK);
		allButtons.add(FILING_SPACE_EVENTS_LINK);
		allButtons.add(BY_FOLDER_EVENTS_LINK);
		allButtons.add(BY_USER_EVENTS_LINK);
		allButtons.add(DECOMMISSIONING_EVENTS_LINK);

		checkExpectedButtons(allButtons, buttonsExpected);
	}

	private void assertThatUserHasNoAccessToRMEvents() {
		//assertThat(buttonByClassIsVisible("logsLink")).isFalse();
		assertThat(navigateToEventListPossible()).isFalse();
	}

	private void assertThatUserInGlobalMenuHasOnlyAccessTo(String... linkExpected) {
		List<String> allLink = new ArrayList<String>();
		allLink.add(RECORDS_MANAGEMENT_LINK);
		allLink.add(ARCHIVES_MANAGEMENT_LINK);
		allLink.add(LOGS_LINK);
		allLink.add(ADMIN_MODULE_LINK);
		allLink.add(USER_DOCUMENTS_LINK);

		checkExpectedButtons(allLink, linkExpected);
	}

	private void checkExpectedButtons(List<String> allButtons, String... buttonsExpected) {
		for (String aButton : allButtons) {

			if (buttonIsExpected(aButton, buttonsExpected)) {
				assertThat(buttonByClassIsVisible(aButton)).describedAs("Button " + aButton + " visibility").isTrue();
			} else {
				assertThat(buttonByClassIsVisible(aButton)).describedAs("Button " + aButton + " visibility").isFalse();
			}
		}
	}

	private boolean buttonIsExpected(String aButton, String... buttonsExpected) {
		for (String aButtonExpected : buttonsExpected) {
			if (aButtonExpected == aButton) {
				return true;
			}
		}
		return false;
	}

	private Boolean buttonByClassIsVisible(String buttonClassName) {
		try {
			driver.findElement(By.className(buttonClassName));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void givenAliceHasPermission(String... permissions) {
		Role role = rolesManager.getRole(zeCollection, "roleDeAlice");
		role = role.withPermissions(asList(permissions));
		rolesManager.updateRole(role);
	}

	private boolean navigateToAdminModulePossible() {
		try {
			driver.navigateTo().url(ADMIN_MODULE);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToArchivesManagementPossible() {
		try {
			driver.navigateTo().url(ARCHIVES_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToCreateFolderPossible() {
		try {
			driver.navigateTo().url(RMNavigationConfiguration.ADD_FOLDER);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToAddDocumentPossible() {
		try {
			driver.navigateTo().url(RMNavigationConfiguration.ADD_DOCUMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	//DOC
	private boolean navigationToFolderPossible(String folderId) {
		try {
			driver.navigateTo().url(DISPLAY_FOLDER + "/" + folderId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToCreateSubFolderPossible(String folderId) {
		try {
			driver.navigateTo().url(RMNavigationConfiguration.ADD_FOLDER + "/parentId%253D" + folderId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToCreateDocumentPossible(String folderId) {
		try {
			driver.navigateTo().url(RMNavigationConfiguration.ADD_DOCUMENT + "/parentId%253D" + folderId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToEditFolderPossible(String folderId) {
		try {
			driver.navigateTo().url(EDIT_FOLDER + "/id%253D" + folderId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToTaskPossible(String taskId) {
		try {
			driver.navigateTo().url(TasksNavigationConfiguration.DISPLAY_TASK + "/" + taskId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToEditTaskPossible(String taskId) {
		try {
			driver.navigateTo().url(TasksNavigationConfiguration.EDIT_TASK + "/id%253D" + taskId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToDocumentPossible(String documentId) {
		try {
			driver.navigateTo().url(DISPLAY_DOCUMENT + "/" + documentId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToEditDocumentPossible(String documentId) {
		try {
			driver.navigateTo().url(EDIT_DOCUMENT + "/id%253D" + documentId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToRecordAuthorizationsPossible(String recordId) {
		try {
			driver.navigateTo().url(LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + recordId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigationToShareContentPossible(String recordId) {
		try {
			driver.navigateTo().url(NavigatorConfigurationService.SHARE_CONTENT + "/" + recordId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	//ARCHIVES

	private boolean navigateToSearchContainerForDecommissioningListPossible(String listId) {
		try {
			driver.navigateTo().url(DECOMMISSIONING_LIST_ADD_EXISTING_CONTAINER + "/" + listId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCreateContainerForDecommissioningListPossible(String listId) {
		try {
			driver.navigateTo().url(DECOMMISSIONING_LIST_ADD_NEW_CONTAINER + "/" + listId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDecommissioningListPossible(String decommissioningList) {
		try {
			driver.navigateTo().url(DECOMMISSIONING + "/" + decommissioningList);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditDecommissioningListPossible(String decommissioningList) {
		try {
			driver.navigateTo().url(DECOMMISSIONING_LIST_EDIT + "/" + decommissioningList);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToBuildDecommissioningListPossible(String buildType) {
		try {
			driver.navigateTo().url(DECOMMISSIONING_LIST_BUILDER + "/" + buildType);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToTransferNoStorageSpaceContainerPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/transferNoStorageSpace/" + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToTransferWithStorageSpaceContainerPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/transferWithStorageSpace/" + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDepositWithStorageSpaceContainerPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/depositWithStorageSpace/" + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDepositNoStorageSpaceContainerPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_ADMIN_UNIT_WITH_CONTAINERS + "/depositNoStorageSpace/" + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToTransferNoStorageSpaceFilingSpacePossible(String unitId, String filingId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/transferNoStorageSpace/" + unitId + "/" + filingId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDepositNoStorageSpaceFilingSpacePossible(String unitId, String filingId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/despositNoStorageSpace/" + unitId
						 + "/" + filingId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToTransferWithStorageSpaceFilingSpacePossible(String unitId, String filingId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/transferWithStorageSpace/"
						 + unitId + "/" + filingId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDepositWithStorageSpaceFilingSpacePossible(String unitId, String filingId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_FILING_SPACE_WITH_CONTAINERS + "/despositWithStorageSpace/"
						 + unitId + "/" + filingId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDisplayContainerPossible(String containerId) {
		try {
			driver.navigateTo()
					.url(DISPLAY_CONTAINER + "/" + containerId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddFacetPossible() {
		try {
			driver.navigateTo().url(NavigatorConfigurationService.ADD_FACET_CONFIGURATION);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDisplayFacetPossible() {
		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		String facetId = getModelLayerFactory().newSearchServices().searchRecordIds(
				new LogicalSearchQuery().setCondition(from(schemas.facetSchemaType()).returnAll())).get(0);
		try {
			driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FACET_CONFIGURATION + "/" + facetId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditFacetPossible() {
		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		String facetId = getModelLayerFactory().newSearchServices().searchRecordIds(
				new LogicalSearchQuery().setCondition(from(schemas.facetSchemaType()).returnAll())).get(0);
		try {
			driver.navigateTo().url(NavigatorConfigurationService.EDIT_FACET_CONFIGURATION + "/" + facetId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListFacetPossible() {
		try {
			driver.navigateTo().url(NavigatorConfigurationService.LIST_FACET_CONFIGURATION);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToOrderFacetPossible() {
		try {
			driver.navigateTo().url(NavigatorConfigurationService.ORDER_FACET_CONFIGURATION);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToReportsPossible() {
		try {
			driver.navigateTo()
					.url(REPORTS);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	//AUDIT
	private boolean navigateToEventListPossible() {
		try {
			driver.navigateTo().url(EVENTS_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEventCategoryPossible(String category) {
		try {
			driver.navigateTo().url(EVENT_CATEGORY + "/" + category);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToADisplayEventPossible() {
		try {
			driver.navigateTo().url(EVENT_DISPLAY
									+ "/id%253D%253BstartDate%253D2015-04-22T11%253A49%253A19.520%253BendDate%253D2015-04-29T11%253A49%253A19."
									+ "520%253BeventType%253Dopen_session%253BeventCategory%253DSYSTEM_USAGE");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	//ADMIN MODULE
	private boolean navigateToTaxonomiesPossible() {
		try {
			driver.navigateTo().url(LIST_TAXONOMY);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditTaxonomyPossible(String code) {
		try {
			driver.navigateTo().url(TAXONOMY_ADD_EDIT + "/taxonomyCode%253D" + code);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddTaxonomyPossible() {
		try {
			driver.navigateTo().url(TAXONOMY_ADD_EDIT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToACategoryPlanDisplayPossible(String categoryId) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253Dplan%253BconceptId%253D"
						 + categoryId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCategoryPlanDisplayWithParentPossible(String categoryId_children,
																	String categoryId_parent) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253Dplan%253BconceptId%253D"
						 + categoryId_children + "%253BparentConceptId%253D" + categoryId_parent);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddTaxonomyConceptPlanPossible() {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/ADD/plan/category_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddTaxonomyConceptPlanWithParentPossible(String categoryId_parent) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/ADD/plan/category_default/"
						 + categoryId_parent);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditTaxonomyConceptPlanPossible(String categoryId) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/EDIT/plan/category_default/" + categoryId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCategoriesPlanPossible() {
		try {
			driver.navigateTo().url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253Dplan");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCategoriesAdministrativeUnitPossible() {
		try {
			driver.navigateTo().url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253DadmUnits");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToACategoryAdministrativeUnitDisplayPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253DadmUnits%253BconceptId%253D"
						 + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCategoryAdministrativeUnitDisplayWithParentPossible(String unitId_children,
																				  String unitId_parent) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_MANAGEMENT + "/taxonomyCode%253DadmUnits%253BconceptId%253D"
						 + unitId_children + "%253BparentConceptId%253D" + unitId_parent);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddTaxonomyConceptAdministrativeUnitPossible() {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/ADD/admUnits/administrativeUnit_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddTaxonomyConceptAdministrativeUnitWithParentPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/ADD/admUnits/administrativeUnit_default/"
						 + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditTaxonomyConceptAdministrativeUnitPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(TAXONOMY_CONCEPT_ADD_EDIT + "/EDIT/admUnits/administrativeUnit_default/"
						 + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAdministrativeUnitAuthorizationsPossible(String unitId) {
		try {
			driver.navigateTo()
					.url(LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + unitId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUniformSubdivisionPossible(String subdivId) {
		try {
			driver.navigateTo().url(DISPLAY_SCHEMA_RECORD + "/" + subdivId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUniformSubdivisionListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/uniformSubdivision_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddUniformSubdivisionPossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/uniformSubdivision_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditUniformSubdivisionPossible(String subdivId) {
		try {
			driver.navigateTo()
					.url(ADD_EDIT_SCHEMA_RECORD + "/uniformSubdivision_default/" + subdivId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListRetentionRulesPossible() {
		try {
			driver.navigateTo().url(LIST_RETENTION_RULES);
			return !isOnHomePage();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean navigateToRetentionRuleDisplayPossible(String ruleId) {
		try {
			driver.navigateTo().url(DISPLAY_RETENTION_RULE + "/" + ruleId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddRetentionRulePossible() {
		try {
			driver.navigateTo().url(ADD_RETENTION_RULE);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditRetentionRulePossible(String ruleId) {
		try {
			driver.navigateTo().url(EDIT_RETENTION_RULE + "/" + ruleId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToValueDomainsPossible() {
		try {
			driver.navigateTo().url(LIST_VALUE_DOMAINS);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToContainerTypeListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/ddvContainerRecordType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToContainerTypePossible(String containerTypeId) {
		try {
			driver.navigateTo().url(DISPLAY_SCHEMA_RECORD + "/" + containerTypeId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditContainerTypePossible(String containerTypeId) {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvContainerRecordType_default/"
									+ containerTypeId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddContainerTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvContainerRecordType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDocumentTypeListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/ddvDocumentType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDocumentTypePossible(String documentTypeId) {
		try {
			driver.navigateTo().url(DISPLAY_SCHEMA_RECORD + "/" + documentTypeId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditDocumentTypePossible(String documentTypeId) {
		try {
			driver.navigateTo()
					.url(ADD_EDIT_SCHEMA_RECORD + "/ddvDocumentType_default/" + documentTypeId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionSecurityPossible() {
		try {
			driver.navigateTo()
					.url(COLLECTION_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToSchemaRecordList(String schemaCode) {
		try {
			driver.navigateTo()
					.url(LIST_SCHEMA_RECORDS + "/" + schemaCode);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToSchemaRecordDisplay(String id) {
		try {
			driver.navigateTo()
					.url(DISPLAY_SCHEMA_RECORD + "/" + id);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionUserList() {
		try {
			driver.navigateTo().url(COLLECTION_USER_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionUser(String userId) {
		try {
			driver.navigateTo().url(COLLECTION_USER + "/" + userId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionUserRoles(String userId) {
		try {
			driver.navigateTo().url(COLLECTION_USER_ROLES + "/" + userId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionGroup(String groupId) {
		try {
			driver.navigateTo().url(COLLECTION_GROUP + "/" + groupId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionGroupRoles(String groupId) {
		try {
			driver.navigateTo().url(COLLECTION_GROUP_ROLES + "/" + groupId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToSchemaRecordEdit(String schemaCode, String id) {
		try {
			driver.navigateTo()
					.url(ADD_EDIT_SCHEMA_RECORD + "/" + schemaCode + "/" + id);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToSchemaRecordAdd(String schemaCode) {
		try {
			driver.navigateTo()
					.url(ADD_EDIT_SCHEMA_RECORD + "/" + schemaCode);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddDocumentTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvDocumentType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToFolderTypeListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/ddvFolderType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddFolderTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvFolderType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToMediumTypeListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/ddvMediumType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToMediumTypePossible(String mediumType) {
		try {
			driver.navigateTo().url(DISPLAY_SCHEMA_RECORD + "/" + mediumType);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditMediumTypePossible(String mediumType) {
		try {
			driver.navigateTo()
					.url(ADD_EDIT_SCHEMA_RECORD + "/ddvMediumType_default/" + mediumType);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddMediumTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvMediumType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToStorageSpaceTypeListPossible() {
		try {
			driver.navigateTo().url(LIST_SCHEMA_RECORDS + "/ddvStorageSpaceType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddStorageSpaceTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_RECORD + "/ddvMediumType_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDisplaySchemaTypePossible() {
		try {
			driver.navigateTo().url(DISPLAY_SCHEMA_TYPE);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListMetadatasPossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA_METADATA + "/" + ADD_EDIT_SCHEMA_METADATA
									+ "/schemaTypeCode%253DcontainerRecord%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditSchemaTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA + "/" + ADD_EDIT_SCHEMA
									+ "/schemaTypeCode%253DcontainerRecord%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddSchemaTypePossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_SCHEMA + "/" + ADD_EDIT_SCHEMA
									+ "/schemaTypeCode%253DcontainerRecord%253BschemaCode%253D");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToEditMetadataPossible() {
		try {
			driver.navigateTo().url(ADD_EDIT_METADATA + "/" + ADD_EDIT_METADATA
									+ "/schemaTypeCode%253DcontainerRecord%253BmetadataCode%253D%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDisplayFormMetadatasPossible() {
		try {
			driver.navigateTo().url(FORM_DISPLAY_FORM + "/" + FORM_DISPLAY_FORM
									+ "/schemaTypeCode%253DcontainerRecord%253BmetadataCode%253D%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToDisplayDetailsMetadatasPossible() {
		try {
			driver.navigateTo().url(EDIT_DISPLAY_FORM + "/" + EDIT_DISPLAY_FORM
									+ "/schemaTypeCode%253DcontainerRecord%253BmetadataCode%253D%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToSearchDisplayFormMetadatasPossible() {
		try {
			driver.navigateTo().url(SEARCH_DISPLAY_FORM + "/" + SEARCH_DISPLAY_FORM
									+ "/searchDisplayForm/schemaTypeCode%253DcontainerRecord%253BmetadataCode%253D%253BschemaCode%253DcontainerRecord_default");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListOngletMetadatasPossible() {
		try {
			driver.navigateTo().url(LIST_ONGLET + "/" + LIST_ONGLET
									+ "/schemaTypeCode%253Dcategory");
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToPermissionsManagementPossible() {
		try {
			driver.navigateTo().url(PERMISSION_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUserListPossible() {
		try {
			driver.navigateTo().url(USER_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToGroupDisplayPossible(String group) {
		try {
			driver.navigateTo().url(GROUP_DISPLAY + "/"
									+ GROUP_LIST
									+ "/globalGroupCode%253D" + group);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToAddSubGroupPossible(String parentGroupId) {
		try {
			driver.navigateTo().url(GROUP_ADD_EDIT + "/"
									+ GROUP_LIST + "/"
									+ GROUP_DISPLAY + "/parentGlobalGroupCode%253D" + parentGroupId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToGroupListPossible() {
		try {
			driver.navigateTo().url(GROUP_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToGroupAddPossible() {
		try {
			driver.navigateTo()
					.url(GROUP_ADD_EDIT + "/" + GROUP_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToGroupEditPossible(String group) {
		try {
			driver.navigateTo().url(GROUP_ADD_EDIT + "/" + GROUP_LIST
									+ "/globalGroupCode%253D" + group);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToLdapPossible() {
		try {
			driver.navigateTo().url(LDAP_CONFIG_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToConfigurationPossible() {
		try {
			driver.navigateTo().url(CONFIG_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUserDisplayPossible(String user) {
		try {
			driver.navigateTo().url(USER_DISPLAY + "/" + USER_LIST
									+ "/username%253D" + user);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUserAddPossible() {
		try {
			driver.navigateTo().url(USER_ADD_EDIT + "/" + USER_LIST);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUserEditPossible(String user) {
		try {
			driver.navigateTo().url(USER_ADD_EDIT + "/" + USER_LIST
									+ "/username%253D" + user);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListCollectionsPossible() {
		try {
			driver.navigateTo().url(COLLECTION_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToListPrincipalAuthorizationsPossible(String userOrGroupId) {
		try {
			driver.navigateTo().url(LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS + "/" + userOrGroupId);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionEditPossible(String collection) {
		try {
			driver.navigateTo().url(COLLECTION_ADD_EDIT + "/"
									+ COLLECTION_MANAGEMENT + "/collectionCode253D" + collection);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToCollectionAddPossible() {
		try {
			driver.navigateTo().url(COLLECTION_ADD_EDIT + "/"
									+ COLLECTION_MANAGEMENT);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToUpdateCenterPossible() {
		try {
			driver.navigateTo().url(UPDATE_MANAGER);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToExportPossible() {
		try {
			driver.navigateTo().url(EXPORTER);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToImportRecordsPossible() {
		try {
			driver.navigateTo().url(IMPORT_FILE);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToImportUsersPossible() {
		try {
			driver.navigateTo().url(IMPORT_USERS);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean navigateToImportSchemaTypesPossible() {
		try {
			driver.navigateTo().url(IMPORT_SCHEMA_TYPES);
			return !isOnHomePage();
		} catch (Exception e) {
			return false;
		}
	}

	private void logAsIn(String user, String collection) {
		driver = newWebDriver(loggedAsUserInCollection(user, collection));
	}

	private boolean isOnHomePage() {
		return driver.getCurrentUrl().endsWith("/constellio/#!/lastViewedFolders");
	}

	//------------------------------------------------------------------------------------------------------

	private void assertThatCanNavigateToAllArchivesPages() {

		assertThatCanNavigateToAllDecommissioningPages();
		assertThatCanNavigateToAllContainersPages();
		assertThat(navigateToReportsPossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllArchivesPages() {
		assertThatCannotNavigateToAllDecommissioningPages();
		assertThatCannotNavigateToAllContainersPages();

		assertThat(navigateToReportsPossible()).isFalse();
	}

	private void assertThatCanNavigateToAllAdminModulePages() {

		assertThatCanNavigateToTaxonomiesPages();
		assertThatCanNavigateToAllClassificationPlanPages();
		assertThatCanNavigateToAllSecurityPages();
		assertThatCanNavigateToAllUniformSubdivisionsPages();
		assertThatCanNavigateToAllRetentionRulesPages();

		assertThatCanNavigateToAllValueDomainPages();
		assertThatCanNavigateToAllMetadataSchemaPages();

		assertThat(navigateToConfigurationPossible()).isTrue();
		assertThat(navigateToLdapPossible()).isTrue();

		assertThatCanNavigateToAllSystemGroupPages();
		assertThatCanNavigateToAllSystemUsersPages();

		assertThat(navigateToUpdateCenterPossible()).isTrue();
		assertThat(navigateToImportRecordsPossible()).isTrue();
		assertThat(navigateToImportSchemaTypesPossible()).isTrue();
		assertThat(navigateToImportUsersPossible()).isTrue();
		assertThat(navigateToExportPossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllAdminModulePages() {

		assertThatCannotNavigateToTaxonomiesPages();
		assertThatCannotNavigateToAllClassificationPlanPages();
		assertThatCannotNavigateToAllSecurityPages();
		assertThatCannotNavigateToAllUniformSubdivisionsPages();
		assertThatCannotNavigateToAllRetentionRulesPages();

		assertThatCannotNavigateToAllValueDomainPages();
		assertThatCannotNavigateToAllMetadataSchemaPages();

		assertThat(navigateToConfigurationPossible()).isFalse();
		assertThat(navigateToLdapPossible()).isFalse();

		assertThatCannotNavigateToAllSystemGroupPages();
		assertThatCannotNavigateToAllSystemUsersPages();
		assertThatCannotNavigateToAllSystemCollectionsPages();

		assertThat(navigateToUpdateCenterPossible()).isFalse();
		assertThat(navigateToImportRecordsPossible()).isFalse();
		assertThat(navigateToImportSchemaTypesPossible()).isFalse();
		assertThat(navigateToImportUsersPossible()).isFalse();
		assertThat(navigateToExportPossible()).isFalse();

		assertThat(navigateToAddFacetPossible()).isFalse();
		assertThat(navigateToEditFacetPossible()).isFalse();
		assertThat(navigateToDisplayFacetPossible()).isFalse();
		assertThat(navigateToListFacetPossible()).isFalse();
		assertThat(navigateToOrderFacetPossible()).isFalse();
	}

	private void assertThatCanNavigateToTaxonomiesPages() {
		assertThat(navigateToTaxonomiesPossible()).isTrue();
		assertThat(navigateToEditTaxonomyPossible(customTaxonomyCode)).isTrue();
		assertThat(navigateToAddTaxonomyPossible()).isTrue();
	}

	private void assertThatCannotNavigateToTaxonomiesPages() {
		assertThat(navigateToTaxonomiesPossible()).isFalse();
		assertThat(navigateToEditTaxonomyPossible(customTaxonomyCode)).isFalse();
		assertThat(navigateToAddTaxonomyPossible()).isFalse();
	}

	private void assertThatCannotNavigateToAllDecommissioningPages() {
		assertThat(navigateToDecommissioningListPossible(records.list_01)).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("fixedPeriod")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("code888")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("code999")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("transfer")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("activeToDeposit")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("activeToDestroy")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("semiActiveToDeposit")).isFalse();
		assertThat(navigateToBuildDecommissioningListPossible("semiActiveToDestroy")).isFalse();

		assertThat(navigateToSearchContainerForDecommissioningListPossible(records.list_04)).isFalse();
		assertThat(navigateToCreateContainerForDecommissioningListPossible(records.list_04)).isFalse();
	}

	private void assertThatCanNavigateToAllDecommissioningPages() {
		assertThat(navigateToDecommissioningListPossible(records.list_01)).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("fixedPeriod")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("code888")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("code999")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("transfer")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("activeToDeposit")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("activeToDestroy")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("semiActiveToDeposit")).isTrue();
		assertThat(navigateToBuildDecommissioningListPossible("semiActiveToDestroy")).isTrue();

		assertThat(navigateToSearchContainerForDecommissioningListPossible(records.list_04)).isTrue();
		//		assertThat(navigateToCreateContainerForDecommissioningListPossible(records.list_04)).isTrue();
	}

	private void assertThatCanNavigateToAllSystemCollectionsPages() {
		assertThat(navigateToListCollectionsPossible()).isTrue();
		assertThat(navigateToCollectionEditPossible(records.getCollection().toString())).isTrue();
		assertThat(navigateToCollectionAddPossible()).isTrue();

		assertThat(navigateToCollectionSecurityPossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllSystemCollectionsPages() {
		assertThat(navigateToListCollectionsPossible()).isFalse();
		assertThat(navigateToCollectionEditPossible(records.getCollection().toString())).isFalse();
		assertThat(navigateToCollectionAddPossible()).isFalse();

		assertThat(navigateToCollectionSecurityPossible()).isFalse();
	}

	private void assertThatCanNavigateToAllSystemUsersPages() {
		assertThat(navigateToUserListPossible()).isTrue();
		assertThat(navigateToUserDisplayPossible(records.getAlice().getFirstName().toLowerCase())).isTrue();
		assertThat(navigateToUserAddPossible()).isTrue();
		assertThat(navigateToUserEditPossible(records.getAlice().getFirstName().toLowerCase())).isTrue();
	}

	private void assertThatCannotNavigateToAllSystemUsersPages() {
		assertThat(navigateToUserListPossible()).isFalse();
		assertThat(navigateToUserDisplayPossible(records.getAlice().getFirstName().toLowerCase())).isFalse();
		assertThat(navigateToUserAddPossible()).isFalse();
		assertThat(navigateToUserEditPossible(records.getAlice().getFirstName().toLowerCase())).isFalse();
	}

	private void assertThatCanNavigateToAllSystemGroupPages() {
		assertThat(navigateToGroupListPossible()).isTrue();
		assertThat(navigateToGroupDisplayPossible(records.getHeroes().getCode())).isTrue();
		assertThat(navigateToAddSubGroupPossible(records.getHeroes().getCode())).isTrue();
		assertThat(navigateToGroupAddPossible()).isTrue();
		assertThat(navigateToGroupEditPossible(records.getHeroes().getCode())).isTrue();
	}

	private void assertThatCannotNavigateToAllSystemGroupPages() {
		assertThat(navigateToGroupListPossible()).isFalse();
		assertThat(navigateToGroupDisplayPossible(records.getHeroes().getCode())).isFalse();
		assertThat(navigateToAddSubGroupPossible(records.getHeroes().getCode())).isFalse();
		assertThat(navigateToGroupAddPossible()).isFalse();
		assertThat(navigateToGroupEditPossible(records.getHeroes().getCode())).isFalse();
	}

	private void assertThatCanNavigateToAllRetentionRulesPages() {
		assertThat(navigateToListRetentionRulesPossible()).isTrue();
		assertThat(navigateToRetentionRuleDisplayPossible(records.ruleId_1)).isTrue();
		assertThat(navigateToAddRetentionRulePossible()).isTrue();
		assertThat(navigateToEditRetentionRulePossible(records.ruleId_1)).isTrue();
	}

	private void assertThatCannotNavigateToAllRetentionRulesPages() {
		assertThat(navigateToListRetentionRulesPossible()).isFalse();
		assertThat(navigateToRetentionRuleDisplayPossible(records.ruleId_1)).isFalse();
		assertThat(navigateToAddRetentionRulePossible()).isFalse();
		assertThat(navigateToEditRetentionRulePossible(records.ruleId_1)).isFalse();
	}

	private void assertThatCanNavigateToAllMetadataSchemaPages() {
		assertThat(navigateToDisplaySchemaTypePossible()).isTrue();
		assertThat(navigateToEditSchemaTypePossible()).isTrue();
		assertThat(navigateToAddSchemaTypePossible()).isTrue();
		assertThat(navigateToListMetadatasPossible()).isTrue();
		assertThat(navigateToListOngletMetadatasPossible()).isTrue();
		assertThat(navigateToSearchDisplayFormMetadatasPossible()).isTrue();
		assertThat(navigateToDisplayDetailsMetadatasPossible()).isTrue();
		assertThat(navigateToEditMetadataPossible()).isTrue();
		assertThat(navigateToDisplayFormMetadatasPossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllMetadataSchemaPages() {
		assertThat(navigateToDisplaySchemaTypePossible()).isFalse();
		assertThat(navigateToEditSchemaTypePossible()).isFalse();
		assertThat(navigateToAddSchemaTypePossible()).isFalse();
		assertThat(navigateToListMetadatasPossible()).isFalse();
		assertThat(navigateToListOngletMetadatasPossible()).isFalse();
		assertThat(navigateToSearchDisplayFormMetadatasPossible()).isFalse();
		assertThat(navigateToDisplayDetailsMetadatasPossible()).isFalse();
		assertThat(navigateToEditMetadataPossible()).isFalse();
		assertThat(navigateToDisplayFormMetadatasPossible()).isFalse();
	}

	private void assertThatCanNavigateToAllValueDomainPages() {
		assertThat(navigateToValueDomainsPossible()).isTrue();

		assertThat(navigateToContainerTypeListPossible()).isTrue();
		assertThat(navigateToContainerTypePossible(records.containerTypeId_boite22x22)).isTrue();
		assertThat(navigateToEditContainerTypePossible(records.containerTypeId_boite22x22)).isTrue();
		assertThat(navigateToAddContainerTypePossible()).isTrue();

		assertThat(navigateToDocumentTypeListPossible()).isTrue();
		assertThat(navigateToDocumentTypePossible(records.documentTypeId_1)).isTrue();
		assertThat(navigateToEditDocumentTypePossible(records.documentTypeId_2)).isTrue();
		assertThat(navigateToAddDocumentTypePossible()).isTrue();

		assertThat(navigateToFolderTypeListPossible()).isTrue();
		assertThat(navigateToAddFolderTypePossible()).isTrue();

		assertThat(navigateToMediumTypeListPossible()).isTrue();
		assertThat(navigateToMediumTypePossible(records.PA)).isTrue();
		assertThat(navigateToEditMediumTypePossible(records.PA)).isTrue();
		assertThat(navigateToAddMediumTypePossible()).isTrue();

		assertThat(navigateToStorageSpaceTypeListPossible()).isTrue();
		assertThat(navigateToAddStorageSpaceTypePossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllValueDomainPages() {
		assertThat(navigateToValueDomainsPossible()).isFalse();

		assertThat(navigateToContainerTypeListPossible()).isFalse();
		assertThat(navigateToContainerTypePossible(records.containerTypeId_boite22x22)).isFalse();
		assertThat(navigateToEditContainerTypePossible(records.containerTypeId_boite22x22)).isFalse();
		assertThat(navigateToAddContainerTypePossible()).isFalse();

		assertThat(navigateToDocumentTypeListPossible()).isFalse();
		assertThat(navigateToDocumentTypePossible(records.documentTypeId_1)).isFalse();
		assertThat(navigateToEditDocumentTypePossible(records.documentTypeId_2)).isFalse();
		assertThat(navigateToAddDocumentTypePossible()).isFalse();

		assertThat(navigateToFolderTypeListPossible()).isFalse();
		assertThat(navigateToAddFolderTypePossible()).isFalse();

		assertThat(navigateToMediumTypeListPossible()).isFalse();
		assertThat(navigateToMediumTypePossible(records.PA)).isFalse();
		assertThat(navigateToEditMediumTypePossible(records.PA)).isFalse();
		assertThat(navigateToAddMediumTypePossible()).isFalse();

		assertThat(navigateToStorageSpaceTypeListPossible()).isFalse();
		assertThat(navigateToAddStorageSpaceTypePossible()).isFalse();
	}

	private void assertThatCanNavigateToAllUniformSubdivisionsPages() {
		assertThat(navigateToUniformSubdivisionPossible(records.subdivId_1)).isTrue();
		assertThat(navigateToUniformSubdivisionListPossible()).isTrue();
		assertThat(navigateToAddUniformSubdivisionPossible()).isTrue();
		assertThat(navigateToEditUniformSubdivisionPossible(records.subdivId_1)).isTrue();
	}

	private void assertThatCannotNavigateToAllUniformSubdivisionsPages() {
		assertThat(navigateToUniformSubdivisionPossible(records.subdivId_1)).isFalse();
		assertThat(navigateToUniformSubdivisionListPossible()).isFalse();
		assertThat(navigateToAddUniformSubdivisionPossible()).isFalse();
		assertThat(navigateToEditUniformSubdivisionPossible(records.subdivId_1)).isFalse();
	}

	private void assertThatCanNavigateToAllClassificationPlanPages() {
		assertThat(navigateToCategoriesPlanPossible()).isTrue();
		assertThat(navigateToACategoryPlanDisplayPossible(records.categoryId_X)).isTrue();
		assertThat(navigateToCategoryPlanDisplayWithParentPossible(records.categoryId_X100, records.categoryId_X)).isTrue();
		assertThat(navigateToAddTaxonomyConceptPlanPossible()).isTrue();
		assertThat(navigateToAddTaxonomyConceptPlanWithParentPossible(records.categoryId_X100)).isTrue();
		assertThat(navigateToEditTaxonomyConceptPlanPossible(records.categoryId_X)).isTrue();
	}

	private void assertThatCannotNavigateToAllClassificationPlanPages() {
		assertThat(navigateToCategoriesAdministrativeUnitPossible()).isFalse();
		assertThat(navigateToACategoryAdministrativeUnitDisplayPossible(records.unitId_20)).isFalse();
		assertThat(navigateToCategoryAdministrativeUnitDisplayWithParentPossible(records.unitId_12, records.unitId_10)).isFalse();
		assertThat(navigateToAddTaxonomyConceptAdministrativeUnitPossible()).isFalse();
		assertThat(navigateToAddTaxonomyConceptAdministrativeUnitWithParentPossible(records.unitId_12)).isFalse();
		assertThat(navigateToEditTaxonomyConceptAdministrativeUnitPossible(records.unitId_30)).isFalse();
		assertThat(navigateToAdministrativeUnitAuthorizationsPossible(records.unitId_30)).isFalse();
	}

	private void assertThatCanNavigateToAllSecurityPages() {
		assertThat(navigateToCollectionUserList()).isTrue();
		assertThat(navigateToCollectionUser(edouardIdInZeCollection)).isTrue();
		assertThat(navigateToCollectionUserRoles(edouardIdInZeCollection)).isTrue();
		assertThat(navigateToCollectionGroup(legendsInZeCollection)).isTrue();
		assertThat(navigateToCollectionGroupRoles(legendsInZeCollection)).isTrue();
		assertThat(navigateToListPrincipalAuthorizationsPossible(edouardIdInZeCollection)).isTrue();
		assertThat(navigateToCategoriesAdministrativeUnitPossible()).isTrue();
		assertThat(navigateToACategoryAdministrativeUnitDisplayPossible(records.unitId_20)).isTrue();
		assertThat(navigateToCategoryAdministrativeUnitDisplayWithParentPossible(records.unitId_12, records.unitId_10)).isTrue();
		assertThat(navigateToAddTaxonomyConceptAdministrativeUnitPossible()).isTrue();
		assertThat(navigateToAddTaxonomyConceptAdministrativeUnitWithParentPossible(records.unitId_12)).isTrue();
		assertThat(navigateToEditTaxonomyConceptAdministrativeUnitPossible(records.unitId_30)).isTrue();
		assertThat(navigateToAdministrativeUnitAuthorizationsPossible(records.unitId_30)).isTrue();

		assertThat(navigateToPermissionsManagementPossible()).isTrue();
	}

	private void assertThatCannotNavigateToAllSecurityPages() {
		assertThat(navigateToCollectionUserList()).isFalse();
		assertThat(navigateToCollectionUser(edouardIdInZeCollection)).isFalse();
		assertThat(navigateToCollectionUserRoles(edouardIdInZeCollection)).isFalse();
		assertThat(navigateToCollectionGroup(legendsInZeCollection)).isFalse();
		assertThat(navigateToCollectionGroupRoles(legendsInZeCollection)).isFalse();
		assertThat(navigateToListPrincipalAuthorizationsPossible(edouardIdInZeCollection)).isFalse();

		assertThat(navigateToPermissionsManagementPossible()).isFalse();
	}

	private void assertThatCanNavigateToAllContainersPages() {
		assertThat(navigateToTransferNoStorageSpaceContainerPossible(records.unitId_11)).isTrue();
		assertThat(navigateToDepositNoStorageSpaceContainerPossible(records.unitId_10)).isTrue();
		assertThat(navigateToTransferWithStorageSpaceContainerPossible(records.unitId_10)).isTrue();
		assertThat(navigateToDepositWithStorageSpaceContainerPossible(records.unitId_10)).isTrue();

		//		assertThat(navigateToTransferNoStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isTrue();
		//		assertThat(navigateToDepositNoStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isTrue();
		//		assertThat(navigateToTransferWithStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isTrue();
		//		assertThat(navigateToDepositWithStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isTrue();
		//		assertThat(navigateToDisplayContainerPossible(records.containerId_bac15)).isTrue();
	}

	private void assertThatCannotNavigateToAllContainersPages() {
		assertThat(navigateToTransferNoStorageSpaceContainerPossible(records.unitId_11)).isFalse();
		assertThat(navigateToDepositNoStorageSpaceContainerPossible(records.unitId_10)).isFalse();
		assertThat(navigateToTransferWithStorageSpaceContainerPossible(records.unitId_10)).isFalse();
		assertThat(navigateToDepositWithStorageSpaceContainerPossible(records.unitId_10)).isFalse();

		//		assertThat(navigateToTransferNoStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isFalse();
		//		assertThat(navigateToDepositNoStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isFalse();
		//		assertThat(navigateToTransferWithStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isFalse();
		//		assertThat(navigateToDepositWithStorageSpaceFilingSpacePossible(records.unitId_10, records.filingId_A)).isFalse();
		//		assertThat(navigateToDisplayContainerPossible(records.containerId_bac15)).isFalse();
	}

	private void assertThatCanNavigateToAllEventPages() {

		assertThat(navigateToEventListPossible()).isTrue();
		assertThat(navigateToADisplayEventPossible()).isTrue();
		assertThat(navigateToEventCategoryPossible(SYSTEM_USAGE)).isTrue();
		assertThat(navigateToEventCategoryPossible(USERS_AND_GROUPS_ADD_OR_REMOVE)).isTrue();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_CREATION)).isTrue();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_MODIFICATION)).isTrue();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_DELETION)).isTrue();
		assertThat(navigateToEventCategoryPossible(CURRENTLY_BORROWED_DOCUMENTS)).isTrue();
		assertThat(navigateToEventCategoryPossible(DOCUMENTS_BORROW_OR_RETURN)).isTrue();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_ADMINISTRATIVE_UNIT)).isTrue();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_FOLDER)).isTrue();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_USER)).isTrue();
		assertThat(navigateToEventCategoryPossible(DECOMMISSIONING_EVENTS)).isTrue();
	}

	private void assertThatCannotNavigateToAllEventPages() {

		assertThat(navigateToEventListPossible()).isFalse();
		assertThat(navigateToADisplayEventPossible()).isFalse();
		assertThat(navigateToEventCategoryPossible(SYSTEM_USAGE)).isFalse();
		assertThat(navigateToEventCategoryPossible(USERS_AND_GROUPS_ADD_OR_REMOVE)).isFalse();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_CREATION)).isFalse();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_MODIFICATION)).isFalse();
		assertThat(navigateToEventCategoryPossible(FOLDERS_AND_DOCUMENTS_DELETION)).isFalse();
		assertThat(navigateToEventCategoryPossible(CURRENTLY_BORROWED_DOCUMENTS)).isFalse();
		assertThat(navigateToEventCategoryPossible(DOCUMENTS_BORROW_OR_RETURN)).isFalse();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_ADMINISTRATIVE_UNIT)).isFalse();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_FOLDER)).isFalse();
		assertThat(navigateToEventCategoryPossible(EVENTS_BY_USER)).isFalse();
		assertThat(navigateToEventCategoryPossible(DECOMMISSIONING_EVENTS)).isFalse();
	}

	private void assertThatCanNavigateToAllActiveRMPages() {
		assertThat(navigationToFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateSubFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToCreateDocumentPossible(records.folder_A13)).isTrue();
		assertThat(navigationToEditFolderPossible(records.folder_A13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(records.folder_A13)).isTrue();
		assertThat(navigationToShareContentPossible(records.folder_A13)).isTrue();

		assertThat(navigationToDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToEditDocumentPossible(documentInA13)).isTrue();
		assertThat(navigationToRecordAuthorizationsPossible(documentInA13)).isTrue();
		assertThat(navigationToShareContentPossible(documentInA13)).isTrue();
	}

}
