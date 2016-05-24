package com.constellio.app.modules.rm.ui.components.document.fields;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class DocumentTypeTextInputDataProviderAcceptTest extends ConstellioTest {

	DocumentTypeTextInputDataProvider documentTypeTextInputDataProvider;
	@Mock CoreViews navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SessionContext sessionContext;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;
	Folder folder;
	RetentionRule retentionRule;
	List<String> documentTypes;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		folder = rmSchemasRecordsServices.getFolder(records.folder_A16);
		retentionRule = rmSchemasRecordsServices.getRetentionRule(folder.getRetentionRule());
		documentTypes = retentionRule.getDocumentTypes();

		documentTypeTextInputDataProvider = new DocumentTypeTextInputDataProvider(getConstellioFactories(), sessionContext,
				records.folder_A16, null);
	}

	@Test
	public void givenDefaultConfigAndFolderWhenGetDataThenSameDataThatRetentionRulesOfGivenFolder()
			throws Exception {

		givenDefaultConfig();

		assertThat(documentTypeTextInputDataProvider.getData("", 0, 10))
				.isEqualTo(documentTypes);
		assertThat(documentTypeTextInputDataProvider.size(""))
				.isEqualTo(3);
	}

	@Test
	public void givenDefaultConfigAndFolderAndTextWhenGetDataThenSameDataThatRetentionRulesOfGivenFolderFiteredByGivenText()
			throws Exception {

		givenDefaultConfig();
		String text = rmSchemasRecordsServices.getDocumentType(documentTypes.get(0)).getTitle();

		assertThat(documentTypeTextInputDataProvider.getData(text, 0, 10))
				.isEqualTo(Arrays.asList(documentTypes.get(0)));
		assertThat(documentTypeTextInputDataProvider.size(text))
				.isEqualTo(1);
	}

	@Test
	public void givenAllDocumentTypesConfigAndFolderWhenGetDataThenAllDocumentTypes()
			throws Exception {

		givenAllDocumentTypesConfig();

		assertThat(documentTypeTextInputDataProvider.size(""))
				.isEqualTo(14);
		assertThat(documentTypeTextInputDataProvider.getData("", 0, 14))
				.contains(records.documentTypeId_1,
						records.documentTypeId_2,
						records.documentTypeId_3,
						records.documentTypeId_4,
						records.documentTypeId_5,
						records.documentTypeId_6,
						records.documentTypeId_7,
						records.documentTypeId_8,
						records.documentTypeId_9,
						records.documentTypeId_10
				);
	}

	@Test
	public void givenForceLimitDocumentTypesConfigAndFolderWhenGetDataThenSameDataThatRetentionRulesOfGivenFolder()
			throws Exception {

		givenForceLimitToSameTypesOfRetentionRuleConfig();

		assertThat(documentTypeTextInputDataProvider.getData("", 0, 10))
				.isEqualTo(documentTypes);
		assertThat(documentTypeTextInputDataProvider.size(""))
				.isEqualTo(3);
	}

	@Test
	public void givenForceLimitDocumentTypesConfigAndFolderWitoutDocumentTypesWhenGetDataThenEmpty()
			throws Exception {

		givenForceLimitToSameTypesOfRetentionRuleConfig();
		retentionRule.setDocumentTypesDetails(new ArrayList<RetentionRuleDocumentType>());
		recordServices.update(retentionRule);

		assertThat(documentTypeTextInputDataProvider.size(""))
				.isEqualTo(0);
	}

	@Test
	public void givenForceLimitDocumentTypesConfigAndFolderWitoutDocumentTypesAndCurrentTypeWhenGetDataThenOnlyCurrentType()
			throws Exception {

		givenForceLimitToSameTypesOfRetentionRuleConfig();
		retentionRule.setDocumentTypesDetails(new ArrayList<RetentionRuleDocumentType>());
		recordServices.update(retentionRule);

		documentTypeTextInputDataProvider = new DocumentTypeTextInputDataProvider(getConstellioFactories(), sessionContext,
				records.folder_A16, records.documentTypeId_1);

		assertThat(documentTypeTextInputDataProvider.size(""))
				.isEqualTo(1);
		assertThat(documentTypeTextInputDataProvider.getData("", 0, 10))
				.isEqualTo(Arrays.asList(records.documentTypeId_1));
	}

	//
	private void givenDefaultConfig() {
		givenConfig(RMConfigs.DOCUMENTS_TYPES_CHOICE, DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES);
	}

	private void givenForceLimitToSameTypesOfRetentionRuleConfig() {
		givenConfig(RMConfigs.DOCUMENTS_TYPES_CHOICE, DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES);
	}

	private void givenAllDocumentTypesConfig() {
		givenConfig(RMConfigs.DOCUMENTS_TYPES_CHOICE, DocumentsTypeChoice.ALL_DOCUMENTS_TYPES);
	}
}
