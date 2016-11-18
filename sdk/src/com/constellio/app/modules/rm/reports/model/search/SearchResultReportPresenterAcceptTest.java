package com.constellio.app.modules.rm.reports.model.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import org.apache.solr.schema.SchemaManager;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class SearchResultReportPresenterAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchResultReportPresenter presenter;
	final String reportTitle = "zReportTitle";
	final List<String> foldersA01AndA02 = new ArrayList<>();
	final String folderSchemaType = Folder.SCHEMA_TYPE;
	private RecordServices recordServices;
	private ReportTestUtils reportTestUtils;
	private LogicalSearchQuery searchQuery;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUserToCollection(userServices.getUserCredential(chuckNorris), zeCollection);

		reportTestUtils = new ReportTestUtils(getModelLayerFactory(), zeCollection, records);

		updateFolderA01Metadata();
		updateFolderA02Metadata();
		updateFolderA03Metadata();

		foldersA01AndA02.clear();
		foldersA01AndA02.add(records.folder_A01);
		foldersA01AndA02.add(records.folder_A02);

		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

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
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		presenter.buildModel(getModelLayerFactory());
	}

	@Test
	public void givenValidReportWhenNoFolderChosenThenGenerateReportWithAllQueryRecords() {
		SearchResultReportPresenter.LIMIT = 1000;
		SearchResultReportPresenter.BATCH_SIZE = 100;
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), new ArrayList<String>(), folderSchemaType,
				zeCollection, chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithAllQueryFolders(model);
	}

	@Test
	public void givenValidReportAndQueryWithResultCountGreaterThanLimitWhenNoFolderChosenThenGenerateFirstQueryRecords() {
		SearchResultReportPresenter.LIMIT = 2;
		SearchResultReportPresenter.BATCH_SIZE = 1;
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), new ArrayList<String>(), folderSchemaType,
				zeCollection, chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void givenValidReportWhenTwoFoldersThenGenerateValidReportWithValidData() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void whenReportWithDisabledMetadataThenGenerateReportWithoutDisabledMetadata() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		reportTestUtils.disableAUserReportMetadata();
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithDisabledMetadata(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateChuckNorrisReportForChuckNorris() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateDefaultReportForBob() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
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
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithBoolean(model);
	}

	@Test
	public void givenReportWithRichTextThenWrittenCorrectly() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Folder folder1 = rm.getFolder(records.folder_A01);
		Folder folder2 = rm.getFolder(records.folder_A02);
		folder1.setKeywords(asList("mot1", "mot2", "mot3"));
		folder2.setKeywords(new ArrayList<String>());
		recordServices.update(folder1.getWrappedRecord());
		recordServices.update(folder2.getWrappedRecord());
		reportTestUtils.addDefaultReportWithMultivalue(reportTitle);
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithMultivalue(model);

		String result = "";
		String result2 = "";
		String text = "Ceci est <b>un</b> <i>test</i> <u>pour</u> L'université Laval.<br><br><ol><li>test1</li><li>test2</li><li>test3<br></li></ol>";
		FileParser fileParser = getAppLayerFactory().getModelLayerFactory().newFileParser();
		InputStream stream = new ByteArrayInputStream(text.toString().getBytes(StandardCharsets.UTF_8));
		try {
			result2 = text.replaceAll("<br>", "\n");
			result2 = result2.replaceAll("\\<[^>]*>","");
			result = fileParser.parse(stream, true).getParsedContent();
		} catch (FileParserException e) {
			//We do nothing, we will return metadataValue
		} finally {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
		System.out.println(result);
		System.out.println(result2);
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
		presenter = new SearchResultReportPresenter(getAppLayerFactory(), asList(records.folder_A01), folderSchemaType, zeCollection,
				admin, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReportWithReference(model);
	}
}