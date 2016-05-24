package com.constellio.app.ui.pages.rm.folder;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.assertj.core.api.StringAssert;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.AdvancedResearchWebElement;
import com.constellio.app.ui.tools.RecordDisplayWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.app.ui.tools.ResearchResultWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveFieldWebElement;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class RecordFormMetadatasAcceptanceTest extends ConstellioTest {

	public static final String METADATA1_FINDER = "folder_default_metadata1";
	public static final String METADATA2_FINDER = "folder_default_metadata2";
	public static final String METADATA1_CONTAINER_FINDER = "containerRecord_default_metadata1";
	public static final String METADATA1_DOCUMENT_FINDER = "document_default_metadata1";

	public static final String FOLDER = "Dossier";
	public static final String CONTAINING = "Contenant";
	public static final String DOCUMENT = "Document";

	RecordFormWebElement zeForm;
	AdvancedResearchWebElement advancedResearch;
	ResearchResultWebElement researchResult;

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	String classificationFinder = "X100";
	String administrativeUnitFinder = "12B";

	String metadata1 = "metadata1";
	String metadata2 = "metadata2";

	String zeValueListSchemaTypeCode;
	String zeTaxonomySchemaTypeCode;

	LocalDate aDate = new LocalDate(2015, 02, 22);
	LocalDate anOtherDate = new LocalDate(2005, 02, 22);
	LocalDateTime aDateTime = new LocalDateTime(2015, 02, 22, 15, 0, 0);
	LocalDateTime anOtherDateTime = new LocalDateTime(2005, 02, 22, 15, 0, 0);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		createValueListAndTaxonomyWithRecords();
	}

	// --------------------------------------------------------------------------------------------------------
	// Record Form

	//TODO Vincent
	//@Test
	public void givenCheckboxesMetadataReferencingAnAdministrativeUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		navigateToAddFolderFormLoggedAs(admin);

		//TODO To be continued...
	}

	//TODO Maxime : Décommenter ce test lorsque firefox installé sur le serveur d'intégration @Test
	public void givenCheckboxesMetadataReferencingUserThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Alice");
		assertThatMetadata2().contains("Alice");

		modifyFolderWithCheckboxMetadata();
		assertThatMetadata1().doesNotContain("Alice").contains("Gandalf");
		assertThatMetadata2().doesNotContain("Alice").contains("Gandalf");

		deleteCheckboxMetadataFromFolder();
		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenCheckboxesMetadataReferencingSomeValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Blue");
		assertThatMetadata2().contains("Blue");

		modifyFolderWithCheckboxMetadata();
		assertThatMetadata1().doesNotContain("Blue").contains("Green");
		assertThatMetadata2().doesNotContain("Blue").contains("Green");

		deleteCheckboxMetadataFromFolder();
		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenCheckboxesMetadataReferencingSomeTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		navigateToAddFolderFormLoggedAs(admin);
		waitUntilICloseTheBrowsers();

		//TODO continue
	}

	//TODO Vincent
	//@Test
	public void givenLookupMetadataReferencingSomeUserUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).addElementByChoosingFirstChoice("Gandalf");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).addElementByChoosingFirstChoice("Chuck");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Gandalf");
		assertThatMetadata2().contains("Chuck");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).modifyElementByChoosingFirstChoice(0, "Bob");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).modifyElementByChoosingFirstChoice(0, "Dakota");
		zeForm.clickSaveButtonAndWaitForPageReload();

		waitUntilICloseTheBrowsers();

		assertThatMetadata1().doesNotContain("Gandalf").contains("Bob");
		assertThatMetadata2().doesNotContain("Chuck").contains("Dakota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();

		assertThatMetadata1().doesNotContain("Gandalf").contains("Bob");
		assertThatMetadata2().doesNotContain("Chuck").contains("Dakota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenLookupMetadataReferencingSomeTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).addElementByChoosingFirstChoice("retrospective");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).addElementByChoosingFirstChoice("review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Sprint retrospective");
		assertThatMetadata2().contains("Sprint review");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).modifyElementByChoosingFirstChoice(0, "Burndown");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).modifyElementByChoosingFirstChoice(0, "retrospective");
		zeForm.clickSaveButtonAndWaitForPageReload();

		waitUntilICloseTheBrowsers();

		assertThatMetadata1().doesNotContain("Sprint retrospective").contains("Burndown chart");
		assertThatMetadata2().doesNotContain("Sprint review").contains("Sprint retrospective");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();

		assertThatMetadata1().doesNotContain("Sprint retrospective").contains("Burndown chart");
		assertThatMetadata2().doesNotContain("Sprint review").contains("Sprint retrospective");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenLookupMetadataReferencingSomeValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).addElementByChoosingFirstChoice("Red");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).addElementByChoosingFirstChoice("Red");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Red");
		assertThatMetadata2().contains("Red");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).modifyElementByChoosingFirstChoice(0, "Blue");
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).modifyElementByChoosingFirstChoice(0, "Green");
		zeForm.clickSaveButtonAndWaitForPageReload();

		waitUntilICloseTheBrowsers();

		assertThatMetadata1().doesNotContain("Red").contains("Blue");
		assertThatMetadata2().doesNotContain("Red").contains("Green");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();

		assertThatMetadata1().doesNotContain("Red").contains("Blue");
		assertThatMetadata2().doesNotContain("Red").contains("Green");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveLookupWebElement(METADATA1_FINDER).remove(0);
		zeForm.getListAddRemoveLookupWebElement(METADATA2_FINDER).remove(0);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenDropDownMetadataReferencingSomeUserUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).add("Gandalf");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).add("Chuck");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Gandalf");
		assertThatMetadata2().contains("Chuck");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).modify(0, "Bob");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).modify(0, "Dakota");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Gandalf").contains("Bob");
		assertThatMetadata2().doesNotContain("Chuck").contains("Dakota");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenDropDownMetadataReferencingSomeTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);

		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).add("Sprint retrospective");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).add("Sprint review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("retrospective");
		assertThatMetadata2().contains("review");

		hideMetadatasAndAssertThatNotVisible();

		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).add("Sprint retrospective");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).add("Sprint review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("retrospective");
		assertThatMetadata2().contains("review");

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenDropDownMetadataReferencingSomeValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).add("Red");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).add("Blue");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Red");
		assertThatMetadata2().contains("Blue");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).modify(0, "Blue");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).modify(0, "Orange");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Red").contains("Blue");
		assertThatMetadata2().doesNotContain("Blue").contains("Orange");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenDropDownMetadataReferencingOneUserUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Admin");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Chuck");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Admin");
		assertThatMetadata2().contains("Chuck");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Gandalf");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Charles");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Admin").contains("Gandalf");
		assertThatMetadata2().doesNotContain("Chuck").contains("Charles");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).getEmptyValue();
		zeForm.getDropDown(METADATA2_FINDER).getEmptyValue();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenDropDownMetadataReferencingOneTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);

		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Burndown");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Burndown chart");
		assertThatMetadata2().contains("Sprint review");

		hideMetadatasAndAssertThatNotVisible();

		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Burndown");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Burndown chart");
		assertThatMetadata2().contains("Sprint review");

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenDropDownMetadataReferencingOneValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Blue");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Green");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Blue");
		assertThatMetadata2().contains("Green");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("Green");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Orange");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Blue").contains("Green");
		assertThatMetadata2().doesNotContain("Green").contains("Orange");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).getEmptyValue();
		zeForm.getDropDown(METADATA2_FINDER).getEmptyValue();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenLookUpMetadataReferencingOneUserUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Bob");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("Gandalf");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Bob");
		assertThatMetadata2().contains("Gandalf");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).clear();
		zeForm.getDropDown(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Admin");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("Chuck");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Bob").contains("Admin");
		assertThatMetadata2().doesNotContain("Gandalf").contains("Chuck");

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenLookUpMetadataReferencingOneTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("retrospective");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("review");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("retrospective");
		assertThatMetadata2().contains("review");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).clear();
		zeForm.getDropDown(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("backlog");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("burndown");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("retrospective").contains("backlog");
		assertThatMetadata2().doesNotContain("review").contains("Burndown");

		hideMetadatasAndAssertThatNotVisible();

	}

	@Test
	public void givenLookUpMetadataReferencingOneValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Red");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("Blue");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Red");
		assertThatMetadata2().contains("Blue");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).clear();
		zeForm.getDropDown(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Green");
		zeForm.getLookupField(METADATA2_FINDER).typeAndSelectFirst("Orange");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Red").contains("Green");
		assertThatMetadata2().doesNotContain("Blue").contains("Orange");

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenRadioButtonMetadataReferencingOneUserUnitThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RADIO_BUTTONS, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Alice");
		assertThatMetadata2().contains("Alice");

		navigateToEditFolder(getFolderId());
		zeForm.getRadioButton(METADATA1_FINDER).toggleContaining("Dakota");
		zeForm.getRadioButton(METADATA2_FINDER).toggleContaining("Chuck");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Alice").contains("Dakota");
		assertThatMetadata2().doesNotContain("Alice").contains("Chuck");

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenRadioButtonMetadataReferencingOneTaxonomyThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RADIO_BUTTONS, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		createFolderWithCheckboxMetadata();
		waitUntilICloseTheBrowsers();

		//TODO continue the test
	}

	@Test
	public void givenRadioButtonMetadataReferencingOneValueListThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RADIO_BUTTONS, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(zeValueListSchemaTypeCode);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Blue");
		assertThatMetadata2().contains("Blue");

		navigateToEditFolder(getFolderId());
		zeForm.getRadioButton(METADATA1_FINDER).toggleContaining("Red");
		zeForm.getRadioButton(METADATA2_FINDER).toggleContaining("Green");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Blue").contains("Red");
		assertThatMetadata2().doesNotContain("Blue").contains("Green");

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenDropDownMetadataEnumMultivalueThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAsEnum(AnEnumClass.class).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("A Value Awesome");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Other");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("A Value Awesome");
		assertThatMetadata2().contains("An Other Value");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER).modify(0, "An Other Value");
		zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER).modify(0, "A Value Awesome");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("A Value Awesome").contains("An Other Value");
		assertThatMetadata2().doesNotContain("An Other Value").contains("A Value Awesome");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveDropDownWebElement(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenCheckboxMetadataEnumMultivalueThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAsEnum(AnEnumClass.class).setMultivalue(true);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Awesome");
		assertThatMetadata2().contains("Awesome");

		modifyFolderWithCheckboxMetadata();
		assertThatMetadata1().doesNotContain("Awesome").contains("Other");
		assertThatMetadata2().doesNotContain("Awesome").contains("Other");

		deleteCheckboxMetadataFromFolder();
		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenRadioButtonMetadataEnumThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RADIO_BUTTONS, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAsEnum(AnEnumClass.class).setMultivalue(false);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Awesome");
		assertThatMetadata2().contains("Awesome");

		navigateToEditFolder(getFolderId());
		zeForm.getRadioButton(METADATA1_FINDER).toggleContaining("Other");
		zeForm.getRadioButton(METADATA2_FINDER).toggleContaining("Other");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Awesome").contains("Other");
		assertThatMetadata2().doesNotContain("Awesome").contains("Other");

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenDropDownMetadataEnumThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAsEnum(AnEnumClass.class).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("A Value Awesome");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("Other");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("A Value Awesome");
		assertThatMetadata2().contains("An Other Value");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).selectItemContainingText("An Other Value");
		zeForm.getDropDown(METADATA2_FINDER).selectItemContainingText("A Value Awesome");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("A Value Awesome").contains("An Other Value");
		assertThatMetadata2().doesNotContain("An Other Value").contains("A Value Awesome");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDropDown(METADATA1_FINDER).getEmptyValue();
		zeForm.getDropDown(METADATA2_FINDER).getEmptyValue();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	//TODO Vincent
	//@Test
	public void givenFieldMetadataNumbersMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).add("3.14");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).add("-3.14");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("3.14");
		assertThatMetadata2().contains("-3.14");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).modifyTo(0, "42");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).modifyTo(0, "-42");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("3.14").contains("42");
		assertThatMetadata2().doesNotContain("3.14").contains("42");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenFieldMetadataNumberThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getTextField(METADATA1_FINDER).setValue("3,14");
		zeForm.getTextField(METADATA2_FINDER).setValue("-3,14");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("3.14");
		assertThatMetadata2().contains("-3.14");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).setValue("42");
		zeForm.getTextField(METADATA2_FINDER).setValue("-42");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("3.14").contains("42");
		assertThatMetadata2().doesNotContain("3.14").contains("42");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).clear();
		zeForm.getTextField(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenFieldMetadataTextMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).add("Nota Bene");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).add("Post Sriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).modifyTo(0, "Bla");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).modifyTo(0, "Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextAreaMetadataTextMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).add("Nota Bene");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).add("Post Sriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).modifyTo(0, "Bla");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).modifyTo(0, "Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	//TODO Maxime : Décommenter ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveRichTextField(METADATA1_FINDER).add("Nota Bene");
		zeForm.getListAddRemoveRichTextField(METADATA2_FINDER).add("Post Scriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveRichTextField(METADATA1_FINDER).modifyTo(0, "Bla");
		zeForm.getListAddRemoveRichTextField(METADATA2_FINDER).modifyTo(0, "Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveRichTextField(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveRichTextField(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenFieldMetadataTextThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getTextField(METADATA1_FINDER).setValue("Nota Bene");
		zeForm.getTextField(METADATA2_FINDER).setValue("Post Scriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).setValue("Bla");
		zeForm.getTextField(METADATA2_FINDER).setValue("Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).clear();
		zeForm.getTextField(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextAreaMetadataTextThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getTextField(METADATA1_FINDER).setValue("Nota Bene");
		zeForm.getTextField(METADATA2_FINDER).setValue("Post Sriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).setValue("Bla");
		zeForm.getTextField(METADATA2_FINDER).setValue("Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).clear();
		zeForm.getTextField(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	//TODO Maxime : Décommenter ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getRichTextField(METADATA1_FINDER).setValue("Nota Bene");
		zeForm.getRichTextField(METADATA2_FINDER).setValue("Post Sriptum");
		completeRequiredField();
		driver.snapshot("before-save");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getRichTextField(METADATA1_FINDER).setValue("Bla");
		zeForm.getRichTextField(METADATA2_FINDER).setValue("Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getRichTextField(METADATA1_FINDER).clearField();
		zeForm.getRichTextField(METADATA2_FINDER).clearField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenFieldMetadataStringMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).add("Nota Bene");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).add("Post Sriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveTextField(METADATA1_FINDER).modifyTo(0, "Bla");
		zeForm.getListAddRemoveTextField(METADATA2_FINDER).modifyTo(0, "Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveTextField(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextFieldMetadataStringThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getTextField(METADATA1_FINDER).setValue("Nota Bene");
		zeForm.getTextField(METADATA2_FINDER).setValue("Post Sriptum");
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("Nota");
		assertThatMetadata2().contains("Post");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).setValue("Bla");
		zeForm.getTextField(METADATA2_FINDER).setValue("Nota Bene");
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("Nota Bene").contains("Bla");
		assertThatMetadata2().doesNotContain("Post Scriptum").contains("Nota");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getTextField(METADATA1_FINDER).clear();
		zeForm.getTextField(METADATA2_FINDER).clear();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextFieldMetadataDatetimeMultivaluesThenOK()
			throws Exception

	{
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(true);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveDateTimeFieldWebElement(METADATA1_FINDER).add(aDateTime);
		zeForm.getListAddRemoveDateTimeFieldWebElement(METADATA2_FINDER).add(anOtherDateTime);
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("2015");
		assertThatMetadata2().contains("2005");

		// Modify
		navigateToEditFolder(getFolderId());

		zeForm.getListAddRemoveDateTimeFieldWebElement(METADATA1_FINDER).modify(0, anOtherDateTime);
		zeForm.getListAddRemoveDateTimeFieldWebElement(METADATA2_FINDER).modify(0, aDateTime);

		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("2015").contains("2005");
		assertThatMetadata2().doesNotContain("2005").contains("2015");
	}

	@Test
	public void givenTextFieldMetadataDatetimeThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDatetimeField(METADATA1_FINDER).setValueWithTime(aDateTime);
		zeForm.getDatetimeField(METADATA2_FINDER).setValueWithTime(anOtherDateTime);
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("2015");
		assertThatMetadata2().contains("2005");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getDatetimeField(METADATA1_FINDER).setValueWithTime(anOtherDateTime);
		zeForm.getDatetimeField(METADATA2_FINDER).setValueWithTime(aDateTime);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("2015").contains("2005");
		assertThatMetadata2().doesNotContain("2005").contains("2015");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDatetimeField(METADATA1_FINDER).setEmpty();
		zeForm.getDatetimeField(METADATA2_FINDER).setEmpty();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextFieldMetadataDateMultivaluesThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(true);
			}
		});

		LocalDate aDate = new LocalDate(2015, 02, 22);
		LocalDate anOtherDate = new LocalDate(2005, 02, 22);

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getListAddRemoveDateFieldWebElement(METADATA1_FINDER).add(aDate);
		zeForm.getListAddRemoveDateFieldWebElement(METADATA2_FINDER).add(anOtherDate);
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("2015");
		assertThatMetadata2().contains("2005");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getListAddRemoveDateFieldWebElement(METADATA1_FINDER).modify(0, anOtherDate);
		zeForm.getListAddRemoveDateFieldWebElement(METADATA2_FINDER).modify(0, aDate);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("2015").contains("2005");
		assertThatMetadata2().doesNotContain("2005").contains("2015");

		// Delete
		navigateToEditFolder(getFolderId());
		deleteMetadata(zeForm.getListAddRemoveDateFieldWebElement(METADATA1_FINDER));
		deleteMetadata(zeForm.getListAddRemoveDateFieldWebElement(METADATA2_FINDER));
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenTextFieldMetadataDateThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(false);
			}
		});

		// Create
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.getDateField(METADATA1_FINDER).setValue(aDate);
		zeForm.getDateField(METADATA2_FINDER).setValue(anOtherDate);
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().contains("2015");
		assertThatMetadata2().contains("2005");

		// Modify
		navigateToEditFolder(getFolderId());
		zeForm.getDateField(METADATA1_FINDER).setValue(anOtherDate);
		zeForm.getDateField(METADATA2_FINDER).setValue(aDate);
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadata1().doesNotContain("2015").contains("2005");
		assertThatMetadata2().doesNotContain("2005").contains("2015");

		// Delete
		navigateToEditFolder(getFolderId());
		zeForm.getDateField(METADATA1_FINDER).setEmpty();
		zeForm.getDateField(METADATA2_FINDER).setEmpty();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThatMetadatasNotVisibleInFolderDetails();

		hideMetadatasAndAssertThatNotVisible();
	}

	@Test
	public void givenCheckboxesMetadataBooleanThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.BOOLEAN).setMultivalue(false);
			}
		});

		createFolderWithCheckboxMetadata();
		assertThat(folderDetails().isVisible(METADATA1_FINDER)).isTrue();
		assertThat(folderDetails().isVisible(METADATA2_FINDER)).isTrue();

		modifyFolderWithCheckboxMetadata();
		assertThatMetadata1().contains("Non");
		assertThatMetadata2().contains("Non");

		hideMetadatasAndAssertThatNotVisible();
	}

	// --------------------------------------------------------------------------------------------------------
	// Batch Processing

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateFieldWebElement().add(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateFieldWebElement().add(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateFieldWebElement().add(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getDateField().setValue(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getDateField().setValue(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getDateField().setValue(aDate);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getDatetimeField().setValueWithTime(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getDatetimeField().setValueWithTime(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getDatetimeField().setValueWithTime(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateTimeFieldWebElement().add(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateTimeFieldWebElement().add(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataDateTimeMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.DATE_TIME).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDateTimeFieldWebElement().add(aDateTime);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("2015");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataStringMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.STRING).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenTextAreaMetadataTextWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextAreaField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenTextAreaMetadataTextWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextAreaField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenTextAreaMetadataTextWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextAreaField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getRichTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getRichTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getRichTextField().setValue("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataTextAreaMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataTextAreaMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataTextAreaMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.TEXTAREA, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveRichTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveRichTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenRichTextMetadataTextMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.RICHTEXT, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.TEXT).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveRichTextFieldWebElement().add("a Value");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("a Value");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("3,14");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("3,14");
		waitUntilICloseTheBrowsers();

		// researchResult.clickSaveButtonAndWaitForPageReload();
		// researchResult.clickFirstResultAndWaitForPageReload();
		// assertThatMetadata1InContainer().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getTextField().setValue("3,14");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("3.14");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("3.14");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenFieldMetadataNumberMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.FIELD, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setType(MetadataValueType.NUMBER).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveTextFieldWebElement().add("3.14");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("3.14");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_FINDER).selectItemContainingText("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_FINDER).selectItemContainingText("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_FINDER).selectItemContainingText("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomyWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomyWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_CONTAINER_FINDER).typeAndSelectFirst("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomyWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_DOCUMENT_FINDER).typeAndSelectFirst("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingATaxonomyMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingATaxonomyMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingATaxonomyMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1InContainer() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingATaxonomyMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1InDocument() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomymMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomyMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingATaxonomyMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder taxonomySchemaType = schemaTypes.getSchemaType(zeTaxonomySchemaTypeCode);
				builder.defineReferencesTo(taxonomySchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Daily scrum");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Daily scrum");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_FINDER).selectItemContainingText("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_CONTAINER_FINDER).selectItemContainingText("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_DOCUMENT_FINDER).selectItemContainingText("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_FINDER).typeAndSelectFirst("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_CONTAINER_FINDER).typeAndSelectFirst("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_DOCUMENT_FINDER).typeAndSelectFirst("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingUsersMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingUsersMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Alice");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingUsersMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_CONTAINER_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Alice");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingUsersMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_DOCUMENT_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Alice");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingUsersMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
				builder.defineReferencesTo(userSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice("Dakota");

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("Dakota");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_FINDER).selectItemContainingText(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_CONTAINER_FINDER).selectItemContainingText(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getDropDown(METADATA1_DOCUMENT_FINDER).selectItemContainingText(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_FINDER).typeAndSelectFirst(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_CONTAINER_FINDER).typeAndSelectFirst(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(false);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getLookupField(METADATA1_DOCUMENT_FINDER).typeAndSelectFirst(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenDropDownMetadataReferencingAdministrativeUnitMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.DROPDOWN, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveDropDownWebElement().add(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingAdministrativeUnitMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingAdministrativeUnitMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_CONTAINER_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1InContainer() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenCheckboxMetadataReferencingAdministrativeUnitMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.CHECKBOXES, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.toggleFirstCheckBoxInBatchProcessPopup(METADATA1_DOCUMENT_FINDER);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		//assertThatMetadata1InDocument() contains the right value
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitMultivaluesWithFolderBatchProcessingThenOK()
			throws Exception {
		updateDefaultFolderSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(FOLDER);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitMultivaluesWithContainerBatchProcessingThenOK()
			throws Exception {
		updateDefaultContainerSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(CONTAINING);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InContainer().contains("12");
	}

	@Test
	//TODO Maxime : Activer ce test lorsque firefox installé sur le serveur d'intégration
	@InDevelopmentTest
	public void givenLookupMetadataReferencingAdministrativeUnitMultivaluesWithDocumentBatchProcessingThenOK()
			throws Exception {
		updateDefaultDocumentSchema(MetadataInputType.LOOKUP, new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder adminUnitSchemaType = schemaTypes.getSchemaType(AdministrativeUnit.SCHEMA_TYPE);
				builder.defineReferencesTo(adminUnitSchemaType).setMultivalue(true);
			}
		});

		researchResult = advancedResearchThenBatchProcessing(DOCUMENT);
		researchResult.setModifyOption(metadata1);
		researchResult.getListAddRemoveLookupWebElement().addElementByChoosingFirstChoice(administrativeUnitFinder);

		researchResult.clickSaveButtonAndWaitForPageReload();
		researchResult.clickFirstResultAndWaitForPageReload();
		assertThatMetadata1InDocument().contains("12");
	}

	// -------------------------------------------------------------------------------------------------------

	private void deleteMetadata(ListAddRemoveFieldWebElement<?> listAddRemoveFieldWebElement) {
		listAddRemoveFieldWebElement.getRemoveButtonWebElement(0).click();
		getConfirmdialogOKButton().click();
	}

	public ConstellioWebElement getConfirmdialogOKButton() {
		return driver.waitUntilElementExist(By.id("confirmdialog-ok-button"));
	}

	private void createFolderWithCheckboxMetadata() {
		navigateToAddFolderFormLoggedAs(admin);
		zeForm.toggleFirstCheckbox(METADATA1_FINDER);
		zeForm.toggleFirstCheckbox(METADATA2_FINDER);
		completeRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();
	}

	private void modifyFolderWithCheckboxMetadata() {
		navigateToEditFolder(getFolderId());
		zeForm.toggleAllCheckbox();
		zeForm.clickSaveButtonAndWaitForPageReload();
	}

	private void deleteCheckboxMetadataFromFolder() {
		navigateToEditFolder(getFolderId());
		zeForm.removeCheckbox();
		zeForm.clickSaveButtonAndWaitForPageReload();
	}

	private void navigateToAddFolderFormLoggedAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, zeCollection));
		driver.navigateTo().url(RMNavigationConfiguration.ADD_FOLDER);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));
	}

	private void navigateToEditFolder(String folderId) {
		driver.navigateTo().url(RMNavigationConfiguration.EDIT_FOLDER + "/id%253D" + folderId);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));
	}

	private void completeRequiredField() {
		zeForm.getTextField("folder_default_title").setValue("Pokemon");
		zeForm.getLookupField("folder_default_categoryEntered").typeAndSelectFirst(classificationFinder);
		zeForm.getDateField("folder_default_openingDate").setValue(new LocalDate(2015, 2, 21));
		zeForm.getDropDown("folder_default_administrativeUnitEntered").typeAndSelectFirst(administrativeUnitFinder);
	}

	private RecordDisplayWebElement folderDetails() {
		return new RecordDisplayWebElement(driver.findElement(By.className("main-component")));
	}

	private ResearchResultWebElement advancedResearchThenBatchProcessing(String researchType) {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		advancedResearch = new AdvancedResearchWebElement(driver);

		return advancedResearch.advancedResearchThenBatchProcessingWith(driver, researchType);
	}

	private String getFolderId() {
		String[] splitUrl = driver.getCurrentUrl().split("displayFolder/");
		return splitUrl[1];
	}

	private void updateDefaultFolderSchema(MetadataInputType type, MetadataBuilderConfigurator configurator) {

		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = schemaManager.modify(zeCollection);
		MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
		MetadataBuilder metadata1Builder = schemaBuilder.create(metadata1);
		MetadataBuilder metadata2Builder = schemaBuilder.create(metadata2);
		configurator.configure(metadata1Builder, typesBuilder);
		configurator.configure(metadata2Builder, typesBuilder);

		try {
			schemaManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		SchemaDisplayConfig schemaConfig = schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA);
		List<String> formMetadatas = new ArrayList<>(schemaConfig.getFormMetadataCodes());
		List<String> displayMetadatas = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		formMetadatas.addAll(asList(Folder.DEFAULT_SCHEMA + "_" + metadata1, Folder.DEFAULT_SCHEMA + "_" + metadata2));
		displayMetadatas.addAll(asList(Folder.DEFAULT_SCHEMA + "_" + metadata1, Folder.DEFAULT_SCHEMA + "_" + metadata2));
		schemasDisplayManager.saveSchema(schemaConfig.withFormMetadataCodes(formMetadatas).withDisplayMetadataCodes(
				displayMetadatas));

		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				Folder.DEFAULT_SCHEMA + "_" + metadata1).withInputType(type));
		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				Folder.DEFAULT_SCHEMA + "_" + metadata2).withInputType(type));

		assertThat(schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA).getFormMetadataCodes()).contains(
				Folder.DEFAULT_SCHEMA + "_" + metadata1,
				Folder.DEFAULT_SCHEMA + "_" + metadata2
		);

		assertThat(schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA).getDisplayMetadataCodes()).contains(
				Folder.DEFAULT_SCHEMA + "_" + metadata1,
				Folder.DEFAULT_SCHEMA + "_" + metadata2
		);

	}

	private void updateDefaultDocumentSchema(MetadataInputType type, MetadataBuilderConfigurator configurator) {

		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = schemaManager.modify(zeCollection);
		MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(Document.DEFAULT_SCHEMA);
		MetadataBuilder metadata1Builder = schemaBuilder.create(metadata1);
		configurator.configure(metadata1Builder, typesBuilder);

		try {
			schemaManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		SchemaDisplayConfig schemaConfig = schemasDisplayManager.getSchema(zeCollection, Document.DEFAULT_SCHEMA);
		List<String> formMetadatas = new ArrayList<>(schemaConfig.getFormMetadataCodes());
		List<String> displayMetadatas = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		formMetadatas.addAll(asList(metadata1));
		displayMetadatas.addAll(asList(metadata1));
		schemasDisplayManager.saveSchema(schemaConfig.withFormMetadataCodes(formMetadatas).withDisplayMetadataCodes(
				displayMetadatas));

		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				Document.DEFAULT_SCHEMA + "_" + metadata1).withInputType(type));

	}

	private void updateDefaultContainerSchema(MetadataInputType type, MetadataBuilderConfigurator configurator) {

		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = schemaManager.modify(zeCollection);
		MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA);
		MetadataBuilder metadata1Builder = schemaBuilder.create(metadata1);
		configurator.configure(metadata1Builder, typesBuilder);

		try {
			schemaManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		SchemaDisplayConfig schemaConfig = schemasDisplayManager.getSchema(zeCollection, ContainerRecord.DEFAULT_SCHEMA);
		List<String> formMetadatas = new ArrayList<>(schemaConfig.getFormMetadataCodes());
		List<String> displayMetadatas = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		formMetadatas.addAll(asList(metadata1));
		displayMetadatas.addAll(asList(metadata1));
		schemasDisplayManager.saveSchema(schemaConfig.withFormMetadataCodes(formMetadatas).withDisplayMetadataCodes(
				displayMetadatas));

		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				ContainerRecord.DEFAULT_SCHEMA + "_" + metadata1).withInputType(type));
	}

	private void hideMetadatasAndAssertThatNotVisible() {
		hideMetadatas();
		navigateToAddFolderFormLoggedAs(admin);
		assertThat(zeForm.isVisible(METADATA1_FINDER)).isFalse();
		assertThat(zeForm.isVisible(METADATA2_FINDER)).isFalse();
	}

	private void hideMetadatas() {
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaDisplayConfig schemaConfig = schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA);
		List<String> formMetadatas = new ArrayList<>(schemaConfig.getFormMetadataCodes());
		List<String> displayMetadatas = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		schemasDisplayManager.saveSchema(schemaConfig.withFormMetadataCodes(formMetadatas).withDisplayMetadataCodes(
				displayMetadatas));
		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				Folder.DEFAULT_SCHEMA + "_" + metadata1).withInputType(MetadataInputType.HIDDEN));
		schemasDisplayManager.saveMetadata(schemasDisplayManager.getMetadata(zeCollection,
				Folder.DEFAULT_SCHEMA + "_" + metadata2).withInputType(MetadataInputType.HIDDEN));
	}

	private void createValueListAndTaxonomyWithRecords()
			throws RecordServicesException {
		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);
		zeValueListSchemaTypeCode = valueListServices.createValueDomain("Colors").getCode();
		zeTaxonomySchemaTypeCode = valueListServices.createTaxonomy("Ze Classification").getSchemaTypes().get(0);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		transaction.add(rm.newValueListItem(zeValueListSchemaTypeCode + "_default").setTitle("Red").setCode("red"));
		transaction.add(rm.newValueListItem(zeValueListSchemaTypeCode + "_default").setTitle("Green").setCode("green"));
		transaction.add(rm.newValueListItem(zeValueListSchemaTypeCode + "_default").setTitle("Blue").setCode("blue"));
		transaction.add(rm.newValueListItem(zeValueListSchemaTypeCode + "_default").setTitle("Orange").setCode("orange"));

		String events = transaction.add(newZeTaxonomyRecord().setCode("events").setTitle("Scrum events")).getId();
		transaction.add(newZeTaxonomyRecord().setCode("planning").setTitle("Sprint planning").setParent(events));
		transaction.add(newZeTaxonomyRecord().setCode("daily").setTitle("Daily scrum").setParent(events));
		transaction.add(newZeTaxonomyRecord().setCode("review").setTitle("Sprint review").setParent(events));
		transaction.add(newZeTaxonomyRecord().setCode("retro").setTitle("Sprint retrospective").setParent(events));

		String artefacts = transaction.add(newZeTaxonomyRecord().setCode("scrum artefact").setTitle("Scrum artefact")).getId();
		transaction.add(newZeTaxonomyRecord().setCode("product-backlog").setTitle("Product backlog").setParent(artefacts));
		transaction.add(newZeTaxonomyRecord().setCode("sprint-backlog").setTitle("Sprint backlog").setParent(artefacts));
		transaction.add(newZeTaxonomyRecord().setCode("burndown-chart").setTitle("Burndown chart").setParent(artefacts));

		recordServices.execute(transaction);
	}

	private HierarchicalValueListItem newZeTaxonomyRecord() {
		return rm.newHierarchicalValueListItem(zeTaxonomySchemaTypeCode + "_default");
	}

	private StringAssert assertThatMetadata1() {
		return assertThat(folderDetails().getValue(METADATA1_FINDER));
	}

	private StringAssert assertThatMetadata1InContainer() {
		return assertThat(folderDetails().getValue(METADATA1_CONTAINER_FINDER));
	}

	private StringAssert assertThatMetadata1InDocument() {
		return assertThat(folderDetails().getValue(METADATA1_DOCUMENT_FINDER));
	}

	private StringAssert assertThatMetadata2() {
		return assertThat(folderDetails().getValue(METADATA2_FINDER));
	}

	private void assertThatMetadatasNotVisibleInFolderDetails() {
		assertThat(folderDetails().isVisible(METADATA1_FINDER)).isFalse();
		assertThat(folderDetails().isVisible(METADATA2_FINDER)).isFalse();
	}

	public enum AnEnumClass implements EnumWithSmallCode {

		AVALUE("A Value Awesome"), ANOTHERVALUE("An Other Value");

		private String code;

		AnEnumClass(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

	}
}
