package com.constellio.app.modules.rm.reports.model.search;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureCalculator;
import com.constellio.app.ui.pages.search.criteria.SearchCriterionTestSetup.TestCalculatedSeparatedStructureFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class SearchResultReportPresenterAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchResultReportPresenter presenter;
	final String reportTitle = "zReportTitle";
	final List<String> foldersA01AndA02 = new ArrayList<>();
	final String folderSchemaType = Folder.SCHEMA_TYPE;
	private RecordServices recordServices;
	private ReportTestUtils reportTestUtils;
	private LogicalSearchQuery searchQuery;
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(chuckNorris, (req) -> req.addToCollection(zeCollection));

		reportTestUtils = new ReportTestUtils(getModelLayerFactory(), zeCollection, records);

		updateFolderA01Metadata();
		updateFolderA02Metadata();
		updateFolderA03Metadata();

		foldersA01AndA02.clear();
		foldersA01AndA02.add(records.folder_A01);
		foldersA01AndA02.add(records.folder_A02);

		schemas = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02, records.folder_A03))).sortAsc(Schemas.TITLE);
	}

	private void updateFolderA01Metadata() {
		try {
			recordServices.update(records.getFolder_A01()
					.setTitle(reportTestUtils.getExpectedFolderTitle_A01())
					.setDescription(reportTestUtils.getExpectedFolderDescription_A01())
					.setCreatedBy(reportTestUtils.getExpectedFolderCreator_A01()));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateFolderA02Metadata() {
		try {
			recordServices.update(records.getFolder_A02()
					.setTitle(reportTestUtils.getExpectedFolderTitle_A02())
					.setDescription(reportTestUtils.getExpectedFolderDescription_A02())
					.setCreatedBy(reportTestUtils.getExpectedFolderCreator_A02()));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void updateFolderA03Metadata() {
		try {
			recordServices.update(records.getFolder_A03()
					.setTitle(reportTestUtils.getExpectedFolderTitle_A03())
					.setDescription(reportTestUtils.getExpectedFolderDescription_A03())
					.setCreatedBy(reportTestUtils.getExpectedFolderCreator_A02()));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	@Test(expected = NoSuchReportRuntimeException.class)
	public void whenInvalidReportThenThrowsNoSuchReportRuntimeException() {
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		presenter.buildModel(getModelLayerFactory());
	}

	//	@Test
	//	public void givenValidReportWhenNoFolderChosenThenGenerateReportWithAllQueryRecords() {
	//		SearchResultReportPresenter.LIMIT = 1000;
	//		SearchResultReportPresenter.BATCH_SIZE = 100;
	//		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
	//		presenter = new SearchResultReportPresenter(getAppLayerFactory(), new ArrayList<String>(), folderSchemaType,
	//				zeCollection, chuckNorris, reportTitle, null, Locale.FRENCH);
	//		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
	//		reportTestUtils.validateUserReportWithAllQueryFolders(model);
	//	}

	//	@Test
	//	public void givenValidReportAndQueryWithResultCountGreaterThanLimitWhenNoFolderChosenThenGenerateFirstQueryRecords() {
	//		SearchResultReportPresenter.LIMIT = 2;
	//		SearchResultReportPresenter.BATCH_SIZE = 1;
	//		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
	//		presenter = new SearchResultReportPresenter(getAppLayerFactory(), new ArrayList<String>(), folderSchemaType,
	//				zeCollection, chuckNorris, reportTitle, null, Locale.FRENCH);
	//		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
	//		reportTestUtils.validateUserReportWithSelectedFolders(model);
	//	}

	@Test
	public void givenValidReportWhenTwoFoldersThenGenerateValidReportWithValidData() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void whenReportWithDisabledMetadataThenGenerateReportWithDisabledMetadata() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		reportTestUtils.disableAUserReportMetadata();
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithDisabledMetadata(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateChuckNorrisReportForChuckNorris() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateDefaultReportForBob() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				bobGratton, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReport(model);
	}

	@Test
	public void givenReportWithMultivalueThenWrittenCorrectly() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		Folder folder1 = rm.getFolder(records.folder_A01);
		Folder folder2 = rm.getFolder(records.folder_A02);
		folder1.setKeywords(asList("mot1", "mot2", "mot3"));
		folder2.setKeywords(new ArrayList<String>());
		recordServices.update(folder1.getWrappedRecord());
		recordServices.update(folder2.getWrappedRecord());
		reportTestUtils.addDefaultReportWithMultivalue(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithMultivalue(model);
	}

	@Test
	public void givenReportWithBooleanThenWrittenCorrectly() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Folder folder1 = rm.getFolder(records.folder_A01);
		Folder folder2 = rm.getFolder(records.folder_A02);

		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		manager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
				Folder folder1 = rm.getFolder(records.folder_A01);
				Folder folder2 = rm.getFolder(records.folder_A02);

				types.getSchema(folder2.getSchemaCode()).getMetadata(Folder.BORROWED).setEnabled(true);
				types.getSchema(folder1.getSchemaCode()).getMetadata(Folder.BORROWED).setEnabled(true);
			}
		});

		folder1.setBorrowed(true);
		folder2.setBorrowed(false);
		recordServices.update(folder1.getWrappedRecord());
		recordServices.update(folder2.getWrappedRecord());
		reportTestUtils.addDefaultReportWithBoolean(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithBoolean(model);
	}

	@Test
	public void givenReportWithSeparatedStructureThenWrittenCorrectly() throws Exception {
		String separatedStructureLocalCode = "separatedStructure";

		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		manager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			types.getSchema(Folder.DEFAULT_SCHEMA)
					.create(separatedStructureLocalCode)
					.defineStructureFactory(TestCalculatedSeparatedStructureFactory.class)
					.defineDataEntry().asCalculated(TestCalculatedSeparatedStructureCalculator.class);
		});

		reindex();

		String separatedStructureCode = Folder.DEFAULT_SCHEMA + "_" + separatedStructureLocalCode;
		reportTestUtils.addDefaultReportWithSeparatedStructure(reportTitle, separatedStructureCode);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), asList(records.folder_A01), folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithSeparateStructure(model, separatedStructureCode);
	}

	@Test
	public void givenReportWithRichTextThenWrittenCorrectly() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		final String richText = "<blockquote><blockquote><b>Ceci </b><i>est </i><u>un </u>tes<sub>t</sub> " +
								"pou<sup>r</sup> l'<strike>université</strike>.<br><hr><br><br><ol><li>1</li><li>2</li>" +
								"<ol><li>2.1</li></ol></ol><ul><li>A</li><li>B</li><ul><li>B.A<br><hr></li></ul></ul><p>" +
								"<br></p></blockquote></blockquote>";
		manager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				Map<Language, String> labels = new HashMap<>();
				labels.put(Language.French, "richText");

				types.getDefaultSchema(Folder.SCHEMA_TYPE).create("richText").setType(MetadataValueType.TEXT).setLabels(labels).setEnabled(true);
			}
		});
		SchemasDisplayManager displayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		MetadataDisplayConfig config = displayManager.getMetadata(zeCollection, Folder.DEFAULT_SCHEMA + "_richText").withInputType(MetadataInputType.RICHTEXT);
		displayManager.saveMetadata(config);
		Folder folder1 = rm.getFolder(records.folder_A01);
		Folder folder2 = rm.getFolder(records.folder_A02);
		folder1.set("richText", richText);
		recordServices.update(folder1.getWrappedRecord());
		recordServices.update(folder2.getWrappedRecord());
		reportTestUtils.addDefaultReportWithRichText(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01, records.folder_A02))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithRichText(model);
	}

	@Test
	public void givenReportWithReferenceThenWrittenCorrectly() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Folder folder1 = rm.getFolder(records.folder_A01);
		Folder folder2 = rm.getFolder(records.folder_A02);
		folder1.setParentFolder(folder2);
		recordServices.update(folder1.getWrappedRecord());
		reportTestUtils.addDefaultReportWithReference(reportTitle);
		searchQuery = new LogicalSearchQuery(from(schemas.folderSchemaType()).where(Schemas.IDENTIFIER)
				.isIn(asList(records.folder_A01))).sortAsc(Schemas.TITLE);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), asList(records.folder_A01), folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithReference(model);
	}
}