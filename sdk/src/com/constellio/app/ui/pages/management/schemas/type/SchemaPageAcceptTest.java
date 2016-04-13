package com.constellio.app.ui.pages.management.schemas.type;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.LookupWebElement;
import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.app.ui.tools.components.basic.DateTimeFieldWebElement;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveTextFieldWebElement;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

@UiTest
@InDevelopmentTest
public class SchemaPageAcceptTest extends ConstellioTest {

	/*-------------------------------------------------------------
		This test does not work with PhantomJS, so it is tagged as
	    InDevelopment so the CI server ignores it. The test is
	    functional when running it on Firefox.
	                                                   - Patrick
	--------------------------------------------------------------- */

	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	RMTestRecords errors = new RMTestRecords(zeCollection);
	MetadataSchemasManager metadataSchemasManager;

	DisplaySchemaPage displaySchemaPage;
	AddEditSchemaPage addEditSchemaPage;
	AddEditSchemaMetadataPage addEditSchemaMetadataPage;
	FormDisplaySearchPage formDisplaySearchPage;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		givenCollection("otherCollection");

		errors.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

		displaySchemaPage = new DisplaySchemaPage(driver);
		addEditSchemaPage = new AddEditSchemaPage(driver);
		addEditSchemaMetadataPage = new AddEditSchemaMetadataPage(driver);
		formDisplaySearchPage = new FormDisplaySearchPage(driver);
	}

	@Test
	public void allTests()
			throws Exception {

		givenAddSchemaPageWhenCancelButtonClickedThenNavigateToDisplaySchema();
		givenAddSchemaPageWhenSaveButtonClickedThenSaveAndNavigateToDisplaySchemaWithNewSchemaInList();

		givenEditSchemaPageWhenCancelButtonClickedThenNavigateToDisplaySchema();
		givenEditSchemaPageWhenSaveButtonClickedThenUpdateAndNavigateToDisplaySchema();

		givenAddSchemaMetadataPageWhenChangeTypeThenVerifyEnableOptions();
		givenEditSchemaMetadataPageThenSomeFieldsAreDisable();

		givenAddFolderSchemaMetadataPageWhenAddBooleanMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddMultivalueTextMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddTextMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddRichTextBoxMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddContentMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddBorrowContentMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddDateMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddDateHourMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddReferenceLookupMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddReferenceDropDownMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddReferenceRadioMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddStringMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddStringUrlMetadataThenOk();
		givenAddFolderSchemaMetadataPageWhenAddNumeroMetadataThenOk();

		//TODO Vincent - Les tests suivants ne passent pas sur phantonjs
		//		givenFormDisplayFormPageWhenMoveElementsToOtherColumnThenOk();
		//		givenFormDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk();
		//		givenFormDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk();
		//
		//		givenDisplayFormPageWhenMoveElementsToOtherColumnThenOk();
		//		givenDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk();
		//		givenDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk();
		//
		//		givenSearchDisplayFormPageWhenMoveElementsToOtherColumnThenOk();
		//		givenSearchDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk();
		//		givenSearchDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk();
		//-----
	}

	private void givenAddSchemaPageWhenCancelButtonClickedThenNavigateToDisplaySchema()
			throws Exception {
		navigateToAddSchemaPage();

		addEditSchemaPage.getCodeElement().setValue("schema1");
		addEditSchemaPage.getTitleElement().setValue("The schema 1");
		addEditSchemaPage.getCancelButton().click();
		addEditSchemaPage.waitForPageReload();

		assertThat(displaySchemaPage.getTableRows()).hasSize(1);
		assertThat(displaySchemaPage.getTableRows().get(0).getText()).doesNotContain("The schema 1");
	}

	private void givenAddSchemaPageWhenSaveButtonClickedThenSaveAndNavigateToDisplaySchemaWithNewSchemaInList() {
		navigateToAddSchemaPage();

		addEditSchemaPage.getCodeElement().setValue("schema1");
		addEditSchemaPage.getTitleElement().setValue("The schema 1");
		addEditSchemaPage.getSaveButton().click();
		addEditSchemaPage.waitForPageReload();

		assertThat(displaySchemaPage.getTableRows()).hasSize(2);
		assertThat(displaySchemaPage.getTableRows().get(0).getText()).contains("The schema 1");
	}

	private void givenEditSchemaPageWhenCancelButtonClickedThenNavigateToDisplaySchema() {
		navigateToEditSchemaPage();

		addEditSchemaPage.getTitleElement().setValue("The schema 1 with modified title");
		addEditSchemaPage.getCancelButton().click();
		addEditSchemaPage.waitForPageReload();

		assertThat(displaySchemaPage.getTableRows()).hasSize(2);
		assertThat(displaySchemaPage.getTableRows().get(0).getText()).doesNotContain("The schema 1 with modified title");
	}

	private void givenEditSchemaPageWhenSaveButtonClickedThenUpdateAndNavigateToDisplaySchema() {
		navigateToEditSchemaPage();

		addEditSchemaPage.getTitleElement().setValue("The schema 1 with modified title");
		addEditSchemaPage.getSaveButton().click();
		addEditSchemaPage.waitForPageReload();

		assertThat(displaySchemaPage.getTableRows()).hasSize(2);
		assertThat(displaySchemaPage.getTableRows().get(0).getText()).contains("The schema 1 with modified title");
	}

	private void givenAddSchemaMetadataPageWhenChangeTypeThenVerifyEnableOptions()
			throws Exception {
		navigateToAddMetadataPage();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("MetadataValueType.boolean"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.text"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly($("MetadataInputType.textarea"), $("MetadataInputType.richtxt"));

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.content"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly($("MetadataInputType.textarea"), $("MetadataInputType.richtxt"));

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.date"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.datetime"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.reference"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isChecked())
				.isFalse();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly($("MetadataInputType.lookup"), $("MetadataInputType.dropdown"), $("MetadataInputType.radio"));
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.reference"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isChecked())
				.isTrue();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly($("MetadataInputType.lookup"), $("MetadataInputType.dropdown"), $("MetadataInputType.checkboxe"));
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.string"));

		//		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
		//				.containsOnly($("MetadataInputType.field"), $("MetadataInputType.url")); // TODO need a fix
		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		// TODO Investigate why searchable and sortable fields are disabled
		//		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isTrue();
		//		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.number"));

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();
	}

	private void givenEditSchemaMetadataPageThenSomeFieldsAreDisable()
			throws Exception {
		navigateToEditFolderMetadataPage();

		assertThat(addEditSchemaMetadataPage.getCodeElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getValueTypeElement().isEnable()).isFalse();
		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isFalse();
	}

	private void givenAddFolderSchemaMetadataPageWhenAddBooleanMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("booleanMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze boolean Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("MetadataValueType.boolean"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getHighlightElement().toggle();
		new CheckboxWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRbooleanMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze boolean Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRbooleanMetadata_s");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.BOOLEAN);
		assertThat(metadata.isDefaultRequirement()).isFalse();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isUndeletable()).isFalse();
		assertThat(metadata.isUniqueValue()).isFalse();
		assertThat(metadata.isUnmodifiable()).isFalse();
		assertThat(metadata.inheritDefaultSchema()).isFalse();
		assertThat(metadata.getDefaultValue()).isEqualTo(true);
	}

	private void givenAddFolderSchemaMetadataPageWhenAddMultivalueTextMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("multipleTextMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze multiple text Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.text"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.textarea"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getHighlightElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRmultipleTextMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze multiple text Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRmultipleTextMetadata_txt");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(metadata.isDefaultRequirement()).isFalse();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isUndeletable()).isFalse();
		assertThat(metadata.isUniqueValue()).isFalse();
		assertThat(metadata.isUnmodifiable()).isFalse();
		assertThat(metadata.inheritDefaultSchema()).isFalse();
	}

	private void givenAddFolderSchemaMetadataPageWhenAddTextMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("textMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze text Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.text"));

		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.textarea"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRtextMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze text Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRtextMetadata_t");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isTrue();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
	}

	private void givenAddFolderSchemaMetadataPageWhenAddRichTextBoxMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("richTextMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze rich text box Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.text"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.richtxt"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRrichTextMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze rich text box Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRrichTextMetadata_txt");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isTrue();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
	}

	private void givenAddFolderSchemaMetadataPageWhenAddContentMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("contentMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze content Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.content"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataValueType.content"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRcontentMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze content Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRcontentMetadata_ss");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.CONTENT);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isTrue();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();

	}

	private void givenAddFolderSchemaMetadataPageWhenAddBorrowContentMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("borrowContentMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze content to borrow Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.content"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.contentcheck"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRborrowContentMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze content to borrow Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRborrowContentMetadata_ss");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.CONTENT);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isTrue();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();

	}

	private void givenAddFolderSchemaMetadataPageWhenAddDateMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("dateMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze date Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.date"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		new DateFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).setValue(dateTime(4, 10, 2014).toLocalDate());
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRdateMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze date Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRdateMetadata_da");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.DATE);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.getDefaultValue()).isEqualTo(dateTime(4, 10, 2014).toLocalDate());

	}

	private void givenAddFolderSchemaMetadataPageWhenAddDateHourMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("dateHourMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze date hour Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.datetime"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		new DateTimeFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).setValue(dateTime(4, 10, 2014));
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRdateHourMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze date hour Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRdateHourMetadata_dt");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.DATE_TIME);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.getDefaultValue()).isEqualTo(dateTime(4, 10, 2014));
	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceLookupMetadataThenOk()
			throws Exception {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst(
				$("AddEditMetadataView.type.reference"));
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.lookup"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().typeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getAutocompleteElement().toggle();
		new LookupWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).listTypeAndSelectFirst("un");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze reference Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("administrativeUnit");
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRreferenceMetadataId_s");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isTrue();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isTrue();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		Record record = record((String) metadata.getDefaultValue());
		assertThat(record.getSchemaCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceDropDownMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceDropDownMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference DropDown Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.reference"));
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.dropdown"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().listTypeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		new LookupWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).typeAndSelectFirst("un");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceDropDownMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze reference DropDown Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("administrativeUnit");
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRreferenceDropDownMetadataId_s");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isTrue();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		Record record = record((String) metadata.getDefaultValue());
		assertThat(record.getSchemaCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceRadioMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceRadioMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference Radio Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.reference"));
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.radio"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().listTypeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		new LookupWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).typeAndSelectFirst("un");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceRadioMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze reference Radio Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("administrativeUnit");
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRreferenceRadioMetadataId_s");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isTrue();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		Record record = record((String) metadata.getDefaultValue());
		assertThat(record.getSchemaCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);

	}

	private void givenAddFolderSchemaMetadataPageWhenAddStringMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("stringMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze string Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst($("AddEditMetadataView.type.string"));
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst($("MetadataInputType.field"));
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		new ListAddRemoveTextFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).add("Default value1");
		new ListAddRemoveTextFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).add("Default value2");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRstringMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze string Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRstringMetadata_ss");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		assertThat((List) metadata.getDefaultValue()).containsOnly("Default value1", "Default value2");
	}

	private void givenAddFolderSchemaMetadataPageWhenAddStringUrlMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("stringURLMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze string URL Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Chaîne de caractères");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("URL");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		new ListAddRemoveTextFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).add("Default value1");
		new ListAddRemoveTextFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).add("Default value2");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRstringURLMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze string URL Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRstringURLMetadata_ss");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		assertThat((List) metadata.getDefaultValue()).containsOnly("Default value1", "Default value2");
	}

	private void givenAddFolderSchemaMetadataPageWhenAddNumeroMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("numeroMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze numéro Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Numéro");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		new TextFieldWebElement(addEditSchemaMetadataPage.getDefaultValueElement()).setValue("3");
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRnumeroMetadata");
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze numéro Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRnumeroMetadata_d");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.NUMBER);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isFalse();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isChildOfRelationship()).isFalse();
		assertThat(metadata.getDefaultValue()).isEqualTo(3.0);
	}

	private void givenFormDisplayFormPageWhenMoveElementsToOtherColumnThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.FORM_DISPLAY_FORM);

		assertThatMoveElementsIsOk();
	}

	private void givenFormDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.FORM_DISPLAY_FORM);

		assertThatWhenCancelButtonClickedIsOk();
	}

	private void givenFormDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.FORM_DISPLAY_FORM);

		assertThatWhenSaveThenOkInPage(NavigatorConfigurationService.FORM_DISPLAY_FORM);
	}

	private void givenDisplayFormPageWhenMoveElementsToOtherColumnThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.EDIT_DISPLAY_FORM);

		assertThatMoveElementsIsOk();
	}

	private void givenDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.EDIT_DISPLAY_FORM);

		assertThatWhenCancelButtonClickedIsOk();
	}

	private void givenDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.EDIT_DISPLAY_FORM);

		assertThatWhenSaveThenOkInPage(NavigatorConfigurationService.EDIT_DISPLAY_FORM);
	}

	private void givenSearchDisplayFormPageWhenMoveElementsToOtherColumnThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.SEARCH_DISPLAY_FORM);

		assertThatMoveElementsIsOk();
	}

	private void givenSearchDisplayFormPageWhenMoveAndElementsToOtherColumnAndCancelThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.SEARCH_DISPLAY_FORM);

		assertThatWhenCancelButtonClickedIsOk();
	}

	private void givenSearchDisplayFormPageWhenMoveAndElementsToOtherColumnAndSaveThenOk()
			throws Exception {
		navigateToPage(NavigatorConfigurationService.SEARCH_DISPLAY_FORM);

		assertThatWhenSaveThenOkInPage(NavigatorConfigurationService.SEARCH_DISPLAY_FORM);
	}

	//----------Utils-------------
	private void assertThatWhenSaveThenOkInPage(String page) {
		String text1 = formDisplaySearchPage.getOptionsElement().getOptions().get(0).getText();
		formDisplaySearchPage.getOptionsElement().selectByIndex(0);
		formDisplaySearchPage.getAddButtonElement().click();
		formDisplaySearchPage.getSaveButton().click();

		navigateToPage(page);

		int lastIndex = formDisplaySearchPage.getSelectionsElement().getOptions().size() - 1;
		assertThat(formDisplaySearchPage.getSelectionsElement().getOptions().get(lastIndex).getText())
				.isEqualTo(text1);
	}

	private void assertThatWhenCancelButtonClickedIsOk() {
		formDisplaySearchPage.getOptionsElement().selectByIndex(0);
		formDisplaySearchPage.getAddButtonElement().click();

		formDisplaySearchPage.getCancelButton().click();

		//TODO fix cancel back page
		//		assertThat(driver.getCurrentPage()).isEqualTo(
		//				NavigatorConfigurationService.FORM_DISPLAY_FORM + "/schemaTypeCode%253Dfolder%253BschemaCode%253Dfolder_default");
	}

	private void assertThatMoveElementsIsOk() {
		String text1 = formDisplaySearchPage.getOptionsElement().getOptions().get(0).getText();
		String text2 = formDisplaySearchPage.getOptionsElement().getOptions().get(2).getText();
		formDisplaySearchPage.getOptionsElement().selectByIndex(0);
		formDisplaySearchPage.getOptionsElement().selectByIndex(2);
		driver.waitForCondition(new ConditionWithTimeout() {
			@Override
			protected boolean evaluate() {
				System.out.println(formDisplaySearchPage.getAddButtonElement().isEnabled());
				return formDisplaySearchPage.getAddButtonElement().isEnabled();
			}
		}, 2000);
		formDisplaySearchPage.getAddButtonElement().click();

		int lastIndex = formDisplaySearchPage.getSelectionsElement().getOptions().size() - 1;
		System.out.println(formDisplaySearchPage.getOptionsElement().getOptions().size());
		System.out.println(formDisplaySearchPage.getSelectionsElement().getOptions().size());
		assertThat(formDisplaySearchPage.getSelectionsElement().getOptions().get(lastIndex - 1).getText())
				.isEqualTo(text1);
		assertThat(formDisplaySearchPage.getSelectionsElement().getOptions().get(lastIndex).getText())
				.isEqualTo(text2);

		formDisplaySearchPage.getSelectionsElement().selectByIndex(lastIndex - 1);
		formDisplaySearchPage.getSelectionsElement().selectByIndex(lastIndex);
		formDisplaySearchPage.getUpButtonElement().click();
		formDisplaySearchPage.getDownButtonElement().click();
		formDisplaySearchPage.getUpButtonElement().click();

		assertThat(formDisplaySearchPage.getSelectionsElement().getOptions().get(lastIndex - 2).getText())
				.isEqualTo(text1);
		assertThat(formDisplaySearchPage.getSelectionsElement().getOptions().get(lastIndex - 1).getText())
				.isEqualTo(text2);

		formDisplaySearchPage.getSelectionsElement().selectByIndex(lastIndex - 1);
		formDisplaySearchPage.getSelectionsElement().selectByIndex(lastIndex - 2);
		formDisplaySearchPage.getRemoveButtonElement().click();

		assertThat(formDisplaySearchPage.getOptionsElement().getOptions().get(0).getText())
				.isEqualTo(text1);
		assertThat(formDisplaySearchPage.getOptionsElement().getOptions().get(2).getText())
				.isEqualTo(text2);
	}

	private void navigateToAddSchemaPage() {
		displaySchemaPage.navigateToDisplaySchemaFolderPage();
		displaySchemaPage.getAddButton().click();
		displaySchemaPage.waitForPageReload();
	}

	private void navigateToEditSchemaPage() {
		displaySchemaPage.navigateToDisplaySchemaFolderPage();
		displaySchemaPage.getEditButtonOnIndex(0).click();
		displaySchemaPage.waitForPageReload();
	}

	private void navigateToAddMetadataPage() {
		navigateToEditFolderSchemaMetadataPage(1);
		clickAddMetadataButton();
	}

	private void navigateToEditFolderMetadataPage() {
		navigateToEditFolderSchemaMetadataPage(6);
		addEditSchemaMetadataPage.getEditButtonOnIndex(0).click();
		addEditSchemaMetadataPage.waitForPageReload();
	}

	private void navigateToAddFolderMetadataPage() {
		navigateToEditFolderSchemaMetadataPage(6);
		clickAddMetadataButton();
	}

	private void navigateToPage(String page) {
		if (page.equals(NavigatorConfigurationService.FORM_DISPLAY_FORM)) {
			navigateToEditFolderSchemaMetadataPage(7);
		} else if (page.equals(NavigatorConfigurationService.EDIT_DISPLAY_FORM)) {
			navigateToEditFolderSchemaMetadataPage(8);
		} else if (page.equals(NavigatorConfigurationService.SEARCH_DISPLAY_FORM)) {
			navigateToEditFolderSchemaMetadataPage(9);
		} else {
			throw new UnsupportedOperationException("Invalid page");
		}
	}

	private void clickAddMetadataButton() {
		addEditSchemaMetadataPage.getAddButton().clickAndWaitForPageReload();
		//		addEditSchemaMetadataPage.waitForPageReload();
	}

	private void navigateToEditFolderSchemaMetadataPage(int index) {
		displaySchemaPage.navigateToDisplaySchemaFolderPage();
		displaySchemaPage.getEditButtonOnIndex(index).clickAndWaitForPageReload();
		//		displaySchemaPage.waitForPageReload();
	}
}
