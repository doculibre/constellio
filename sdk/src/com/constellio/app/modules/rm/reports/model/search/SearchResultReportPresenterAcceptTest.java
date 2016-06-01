package com.constellio.app.modules.rm.reports.model.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		presenter.buildModel(getModelLayerFactory());
	}

	@Test
	public void givenValidReportWhenNoFolderChosenThenGenerateReportWithAllQueryRecords() {
		SearchResultReportPresenter.LIMIT = 1000;
		SearchResultReportPresenter.BATCH_SIZE = 100;
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), new ArrayList<String>(), folderSchemaType,
				zeCollection, chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithAllQueryFolders(model);
	}

	@Test
	public void givenValidReportAndQueryWithResultCountGreaterThanLimitWhenNoFolderChosenThenGenerateFirstQueryRecords() {
		SearchResultReportPresenter.LIMIT = 2;
		SearchResultReportPresenter.BATCH_SIZE = 1;
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), new ArrayList<String>(), folderSchemaType,
				zeCollection, chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void givenValidReportWhenTwoFoldersThenGenerateValidReportWithValidData() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void whenReportWithDisabledMetadataThenGenerateReportWithoutDisabledMetadata() {
		reportTestUtils.addUserReport(reportTitle, records.getChuckNorris().getUsername());
		reportTestUtils.disableAUserReportMetadata();
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithDisabledMetadata(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateChuckNorrisReportForChuckNorris() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				chuckNorris, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateUserReportWithSelectedFolders(model);
	}

	@Test
	public void givenChuckNorrisReportAndDefaultReportThenGenerateDefaultReportForBob() {
		reportTestUtils.addUserReport(reportTitle, chuckNorris);
		reportTestUtils.addDefaultReport(reportTitle);
		presenter = new SearchResultReportPresenter(getModelLayerFactory(), foldersA01AndA02, folderSchemaType, zeCollection,
				bobGratton, reportTitle, searchQuery, Locale.FRENCH);
		SearchResultReportModel model = presenter.buildModel(getModelLayerFactory());
		reportTestUtils.validateDefaultReport(model);
	}
}