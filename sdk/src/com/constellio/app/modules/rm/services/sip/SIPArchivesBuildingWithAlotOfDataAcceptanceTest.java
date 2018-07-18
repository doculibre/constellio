package com.constellio.app.modules.rm.services.sip;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPBuildAsyncTask;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class SIPArchivesBuildingWithAlotOfDataAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private SearchServices searchServices;
	private RMSchemasRecordsServices rm;
	private IOServices ioServices;

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withRMTest(records).withConstellioRMModule().withConstellioESModule()
						.withDocumentsHavingContent().withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
						.withAllTestUsers()
		);
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
	}

	@Test
	public void createSIPArchiveWithAllDefaultRecordFolders()
			throws Exception {
		String testfileName = "testFileName";
		SIPBuildAsyncTask task = new SIPBuildAsyncTask(testfileName, Collections.singletonList("test"),
				Collections.<String>emptyList(), getAllFolderFromRecordsAsString(), false, records.getAdmin().getUsername(),
				false, getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
		getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(
				new AsyncTaskCreationRequest(task, zeCollection,
						"SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPArchivesBuildingWithAlotOfData"));
		waitForBatchProcess();

		MetadataSchema sipArchiveSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(SIParchive.SCHEMA_TYPE).getCustomSchema(SIParchive.SCHEMA_NAME);
		LogicalSearchCondition allCondition = LogicalSearchQueryOperators.from(sipArchiveSchema).where(ALL);
		SIParchive records = rm.wrapSIParchive(searchServices.searchSingleResult(allCondition));
		Content zipContent = records.getContent();
		InputStream is = getModelLayerFactory().getContentManager()
				.getContentInputStream(zipContent.getLastMajorContentVersion().getHash(),
						"com.constellio.app.modules.rm.services.sip.ConstellioSIPObjectsProviderAcceptanceTest.testSIPGenerationWithEmail");
		nonConventionalClosingZipInputStream zis = new nonConventionalClosingZipInputStream(is);

		ZipEntry ze;
		ze = zis.getNextEntry();
		Namespace currentNameSpace = Namespace.getNamespace("urn:isbn:1-931666-22-9");
		SAXBuilder builder = new SAXBuilder();
		int compteur = 0;
		List<String> names = new ArrayList<>();
		while (ze != null) {
			if (ze.getName().endsWith(".xml")) {
				compteur++;
				names.add(ze.getName());
			}
			ze = zis.getNextEntry();
		}
		//assertThat(compteur).isEqualTo(getTotalOfDocumentForFolderList(getAllFolderFromRecords()) + getAllFolderFromRecords().size());
		zis.doClose();
		ioServices.closeQuietly(is);
	}

	@Test
	public void testCreateArchiveWithAllPossibleFolder()
			throws Exception {
		MetadataSchemaType folderMetadataSchemaType = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType(Folder.SCHEMA_TYPE);
		List<String> allPossibleFolder = searchServices
				.searchRecordIds(new LogicalSearchQuery(from(folderMetadataSchemaType).where(ALL)));

		String testfileName = "testFileName";
		SIPBuildAsyncTask task = new SIPBuildAsyncTask(testfileName, Collections.singletonList("test"),
				Collections.<String>emptyList(), allPossibleFolder, false, records.getAdmin().getUsername(), false,
				getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
		getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(
				new AsyncTaskCreationRequest(task, zeCollection,
						"SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPArchivesBuildingWithAlotOfData"));
		waitForBatchProcess();
	}

	private List<Document> getAllDocumentFromRecords()
			throws Exception {
		List<Document> allDocuments = new ArrayList<>();
		for (Field field : RMTestRecords.class.getDeclaredFields()) {
			if (field.getName().matches("(document_)([A-b](\\d+))")) {
				Method method = RMTestRecords.class
						.getMethod("getDocumentWithContent_" + ((String) field.get(records)).replace("doc", ""));
				allDocuments.add(((Document) method.invoke(records)));
			}
		}
		return allDocuments;
	}

	private List<String> getAllDocumentFromRecordsAsString()
			throws Exception {
		List<String> allDocuments = new ArrayList<>();
		for (Field field : RMTestRecords.class.getDeclaredFields()) {
			if (field.getName().matches("(document_)([A-b](\\d+))")) {
				Method method = RMTestRecords.class
						.getMethod("getDocumentWithContent_" + ((String) field.get(records)).replace("doc", ""));
				allDocuments.add(((Document) method.invoke(records)).getId());
			}
		}
		return allDocuments;
	}

	private List<Folder> getAllFolderFromRecords()
			throws Exception {
		List<Folder> allFolder = new ArrayList<>();
		for (Field field : RMTestRecords.class.getDeclaredFields()) {
			if (field.getName().matches("(folder_)([A-b](\\d+))")) {
				Method method = RMTestRecords.class.getMethod("getFolder_" + field.get(records));
				allFolder.add(((Folder) method.invoke(records)));
			}
		}
		return allFolder;
	}

	private List<String> getAllFolderFromRecordsAsString()
			throws Exception {
		List<String> allFolder = new ArrayList<>();
		for (Field field : RMTestRecords.class.getDeclaredFields()) {
			if (field.getName().matches("(folder_)([A-b](\\d+))")) {
				Method method = RMTestRecords.class.getMethod("getFolder_" + field.get(records));
				allFolder.add(((Folder) method.invoke(records)).getId());
			}
		}
		return allFolder;
	}

	private int getTotalOfDocumentForFolderList(List<Folder> folders) {
		int ammount = 0;
		MetadataSchemaType documentSchemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Document.SCHEMA_TYPE);
		for (Folder folder : folders) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType)
					.where(documentSchemaType.getDefaultSchema().getMetadata(Document.FOLDER)).isEqualTo(folder.getId());
			ammount += searchServices.getResultsCount(new LogicalSearchQuery(condition));
		}
		return ammount;
	}

	private class nonConventionalClosingZipInputStream extends ZipInputStream {

		public nonConventionalClosingZipInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close()
				throws IOException {
			//nothing
		}

		public void doClose()
				throws IOException {
			super.close();
		}

	}
}
