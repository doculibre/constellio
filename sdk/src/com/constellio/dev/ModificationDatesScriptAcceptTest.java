package com.constellio.dev;

import com.constellio.app.extensions.api.scripts.ResetRMModificationDatesFromEventsScript;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.data.conf.DigitSeparatorMode;
import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.conf.PropertiesDataLayerConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ModificationDatesScriptAcceptTest extends ConstellioTest {
	public static final LocalDate DEFAULT_OPENING_DATE = new LocalDate(2001, 01, 05);
	public static final String FOLDER_TITLE = "FolderTest";
	public static final String FOLDER_ID = "folderTest";
	public static final int SIZE_OF_CREATED_CONTENTS = 50;

	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	SearchServices searchServices;
	ContentManager contentManager;

	@Before
	public void setUp()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration configuration) {
				configuration.setHashingEncoding(HashingEncoding.BASE64);
				configuration.setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode.TWO_DIGITS);
			}
		});

		//        TODO Francis : givenDocumentsWithContentWhenMigrateVaultThenContentsExistInVault ne roule pas avec prepare system
		prepareSystemWithoutHyperTurbo(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers().withRMTest(records)
		);

		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		givenSystemLanguageIs("fr");
		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();

		recordServices = getModelLayerFactory().newRecordServices();

	}

	public void createDocuments()
			throws IOException, RecordServicesException {
		Folder folder = buildDefaultFolder();
		File file;
		Content content;
		recordServices.add(folder.getWrappedRecord());

		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			Document document = rm.newDocument();
			document.setFolder(folder);
			file = newTempFileWithContent("document" + index + ".txt", "This is content " + index);
			content = contentManager.createMajor(records.getAdmin(), file.getName(), contentManager.upload(file));
			document.setContent(content);
			recordsList.add(document.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public void createTasks()
			throws Exception {
		File file;
		Content content;
		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			Task task = rm.newRMTask();
			file = newTempFileWithContent("task" + index + ".txt", "This is content of task " + index);
			content = contentManager.createMajor(records.getAdmin(), file.getName(), contentManager.upload(file));
			task.setContent(asList(content));
			recordsList.add(task.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public void createPrintable()
			throws Exception {
		File file;
		Content content;
		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			Printable printable = rm.newPrintable();
			file = newTempFileWithContent("printable" + index + ".jasper", "This is content of printable " + index);
			content = contentManager.createMajor(records.getAdmin(), file.getName(), contentManager.upload(file));
			printable.setJasperFile(content);
			recordsList.add(printable.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public void createDocumentTypes()
			throws Exception {
		File file;
		Content content;
		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			DocumentType documentType = rm.newDocumentType().setCode("code" + index);
			file = newTempFileWithContent("documentType" + index + ".txt", "This is content of documentType " + index);
			content = contentManager.createMajor(records.getAdmin(), file.getName(), contentManager.upload(file));
			documentType.setTemplates(asList(content));
			recordsList.add(documentType.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public void createDecommissioningLists()
			throws Exception {
		File documentReport, folderReport;
		Content documentReportContent, folderReportContent;
		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			DecommissioningList decommissioningList = rm.newDecommissioningList();
			documentReport = newTempFileWithContent("decommissioningListDocument" + index + ".txt",
					"This is content of document report " + index);
			folderReport = newTempFileWithContent("decommissioningListFolder" + index + ".txt",
					"This is content of folder report " + index);
			documentReportContent = contentManager
					.createMajor(records.getAdmin(), documentReport.getName(), contentManager.upload(documentReport));
			folderReportContent = contentManager
					.createMajor(records.getAdmin(), folderReport.getName(), contentManager.upload(folderReport));
			decommissioningList.setDocumentsReportContent(documentReportContent);
			decommissioningList.setFoldersReportContent(folderReportContent);
			recordsList.add(decommissioningList.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public void createTemporaryRecord()
			throws Exception {
		File file;
		Content content;
		List<Record> recordsList = new ArrayList<>();
		for (int index = 1; index <= SIZE_OF_CREATED_CONTENTS; index++) {
			TemporaryRecord temporaryRecord = rm.newTemporaryRecord();
			file = newTempFileWithContent("temporaryRecord" + index + ".txt", "This is content of temporaryRecord " + index);
			content = contentManager.createMajor(records.getAdmin(), file.getName(), contentManager.upload(file));
			temporaryRecord.setContent(content);
			recordsList.add(temporaryRecord.getWrappedRecord());
		}
		Transaction transaction = new Transaction();
		transaction.addAll(recordsList);
		recordServices.execute(transaction);
	}

	public Folder buildDefaultFolder() {
		return rm.newFolderWithId(FOLDER_ID).setTitle(FOLDER_TITLE).setAdministrativeUnitEntered(records.getUnit10())
				.setRetentionRuleEntered(records.getRule1()).setOpenDate(DEFAULT_OPENING_DATE)
				.setCategoryEntered(records.categoryId_X);
	}

	@Test
	public void givenDocumentsWithContentWhenImproveHashCodesThenHashesAreReplaced()
			throws Exception {
		createDocuments();
		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes("zeCollection");
		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes("zeCollection").getSchemaType("document");
		ResetRMModificationDatesFromEventsScript.execute(getAppLayerFactory());

		//		List<Record> newRecords = searchServices.search(new LogicalSearchQuery(from(schemaType).returnAll()));
		//		for (Record record : newRecords) {
		//			Document document = new Document(record, metadataSchemaTypes);
		//			for (String hash : document.getContent().getHashOfAllVersions()) {
		//				assertThat(hash).doesNotContain("/");
		//				assertThat(hash).doesNotContain("+");
		//			}
		//		}
	}
}
