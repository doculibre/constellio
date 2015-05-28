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
package com.constellio.app.ui.pages.management.schemas.type;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

@UiTest
public class SchemaPageAcceptTest extends ConstellioTest {

	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);
	MetadataSchemasManager metadataSchemasManager;

	DisplaySchemaPage displaySchemaPage;
	AddEditSchemaPage addEditSchemaPage;
	AddEditSchemaMetadataPage addEditSchemaMetadataPage;
	FormDisplaySearchPage formDisplaySearchPage;

	@Before
	public void setUp()
			throws Exception {
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		givenCollection("otherCollection");

		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

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

		//TODO Vincent - Le test suivant ne passe pas sur phantonjs
		//		givenAddSchemaMetadataPageWhenChangeTypeThenVerifyEnableOptions();
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

		AutocompleteWebElement valueTypeElement = addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Booléen");

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

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Texte");

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
				.containsOnly("Zone de texte", "Zone de texte riche");

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Contenu");

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly("Contenu", "Contenu Emprunt/Retour");

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Date");

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

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Date & Heure");

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

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Référence");

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isChecked())
				.isFalse();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly("Lookup", "Menu déroulant", "Button radio");
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Référence");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();

		assertThat(addEditSchemaMetadataPage.getMultivalueElement().isChecked())
				.isTrue();
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly("Lookup", "Menu déroulant", "Case à cocher");
		assertThat(addEditSchemaMetadataPage.getAutocompleteElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getHighlightElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getFacetElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getAdvancedSearchElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getSortableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getSearchableElement().isEnabled()).isFalse();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isEnabled()).isTrue();
		assertThat(addEditSchemaMetadataPage.getEnableElement().isChecked()).isTrue();
		assertThat(addEditSchemaMetadataPage.getRequiredElement().isEnabled()).isTrue();

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("String");
		assertThat(addEditSchemaMetadataPage.getEntryElement().getListValues())
				.containsOnly("Champs de base", "URL");

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

		valueTypeElement.clear();
		valueTypeElement.typeAndSelectFirst("Numéro");

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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Booléen");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getHighlightElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRbooleanMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze boolean Metadata");
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
	}

	private void givenAddFolderSchemaMetadataPageWhenAddMultivalueTextMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("multipleTextMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze multiple text Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Texte");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Zone de texte");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getHighlightElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRmultipleTextMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze multiple text Metadata");
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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Texte");

		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Zone de texte");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRtextMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze text Metadata");
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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Texte");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Zone de texte riche");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRrichTextMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze rich text box Metadata");
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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Contenu");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Contenu");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRcontentMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze content Metadata");
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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Contenu");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Contenu Emprunt");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSearchableElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRborrowContentMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze content to borrow Metadata");
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
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Date");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();

		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRdateMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze date Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRdateMetadata_das");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.DATE);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();

	}

	private void givenAddFolderSchemaMetadataPageWhenAddDateHourMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("dateHourMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze date hour Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Date & Heure");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRdateHourMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze date hour Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRdateHourMetadata_dts");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.DATE_TIME);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();

	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceLookupMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Référence");
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Lookup");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().typeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getAutocompleteElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze reference Metadata");
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
	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceDropDownMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceDropDownMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference DropDown Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Référence");
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Menu déroulant");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().typeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceDropDownMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze reference DropDown Metadata");
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
	}

	private void givenAddFolderSchemaMetadataPageWhenAddReferenceRadioMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("referenceRadioMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze reference Radio Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Référence");
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Bouton radio");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getReferenceElement().typeAndSelectFirst("Unité administrative");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getFacetElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRreferenceRadioMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze reference Radio Metadata");
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

	}

	private void givenAddFolderSchemaMetadataPageWhenAddStringMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("stringMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze string Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Chaîne de caractères");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getEntryElement().typeAndSelectFirst("Champs de base");
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRstringMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze string Metadata");
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
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRstringURLMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze string URL Metadata");
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
	}

	private void givenAddFolderSchemaMetadataPageWhenAddNumeroMetadataThenOk() {
		navigateToAddFolderMetadataPage();

		addEditSchemaMetadataPage.getCodeElement().setValue("numeroMetadata");
		addEditSchemaMetadataPage.getTitleElement().setValue("Ze numéro Metadata");
		addEditSchemaMetadataPage.getValueTypeElement().typeAndSelectFirst("Numéro");
		addEditSchemaMetadataPage.getMultivalueElement().toggle();
		addEditSchemaMetadataPage.getMetadataGroupElement().toggle("Default");
		addEditSchemaMetadataPage.getRequiredElement().toggle();
		addEditSchemaMetadataPage.getSaveButton().click();
		addEditSchemaMetadataPage.waitForPageReload();

		Metadata metadata = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getMetadata("folder_default_USRnumeroMetadata");
		assertThat(metadata.getLabel()).isEqualTo("Ze numéro Metadata");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.MANUAL);
		assertThat(metadata.getAllowedReferences()).isNull();
		assertThat(metadata.getDataStoreCode()).isEqualTo("USRnumeroMetadata_ds");
		assertThat(metadata.getType()).isEqualTo(MetadataValueType.NUMBER);
		assertThat(metadata.isDefaultRequirement()).isTrue();
		assertThat(metadata.isEnabled()).isTrue();
		assertThat(metadata.isSystemReserved()).isFalse();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.isSchemaAutocomplete()).isFalse();
		assertThat(metadata.isSearchable()).isFalse();
		assertThat(metadata.isSortable()).isFalse();
		assertThat(metadata.isTaxonomyRelationship()).isFalse();
		assertThat(metadata.isChildOfRelationship()).isFalse();
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

		//TODO Thiago fix cancel back page
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
		addEditSchemaMetadataPage.getAddButton().click();
		addEditSchemaMetadataPage.waitForPageReload();
	}

	private void navigateToEditFolderSchemaMetadataPage(int index) {
		displaySchemaPage.navigateToDisplaySchemaFolderPage();
		displaySchemaPage.getEditButtonOnIndex(index).click();
		displaySchemaPage.waitForPageReload();
	}
}
