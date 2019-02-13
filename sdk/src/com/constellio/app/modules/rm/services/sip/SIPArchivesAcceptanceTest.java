package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.zip.FileSIPZipWriter;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPBuildAsyncTask;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.asList;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;

public class SIPArchivesAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	File outFile;
	IOServices ioServices;
	ContentManager contentManager;
	RecordServices recordServices;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		InputStream bagInfoIn = new FileInputStream(getTestResourceFile("baginfo.txt"));
		List<String> bagInfoLines = IOUtils.readLines(bagInfoIn);
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		outFile = ioServices.newTemporaryFile("temporarySIPFile.zip");
		bagInfoIn.close();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		DefaultSIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), FRENCH);
		bagInfoFactory.setHeaderLines(bagInfoLines);
		RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP = new RMSelectedFoldersAndDocumentsSIPBuilder(zeCollection, getAppLayerFactory());
		FileSIPZipWriter writer = new FileSIPZipWriter(getAppLayerFactory(), outFile, outFile.getName(), bagInfoFactory);
		constellioSIP.buildWithFoldersAndDocuments(writer, asList(records.folder_A01), new ArrayList<String>(), null);

	}

	@Test
	public void checkIfZipContainsTheCorrectFoldersForCategoryTest()
			throws Exception {
		FileInputStream fis = new FileInputStream(outFile);
		ZipInputStream zis = new ZipInputStream(fis);
		Stack<String> categoryStack = new Stack<>();
		Category category = rm.getCategory(records.getFolder_A01().getCategory());
		while (category != null) {
			try {
				categoryStack.push(category.getCode());
				category = rm.getCategory(category.getParent());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				category = null;
			}
		}
		categoryStack.push(records.getFolder_A01().getId());
		ZipEntry ze;
		ze = zis.getNextEntry();
		while (ze != null) {
			String[] path = ze.getName().split("/");
			for (String currentPath : path) {
				//Check if it's the data folder or the file itself
				if (!"data".equals(currentPath) && !currentPath.matches("([A-Za-z0-9\\-])+(\\.)(\\w+)")) {
					assertThat(currentPath).isIn(categoryStack);
				}
			}
			ze = zis.getNextEntry();
		}
		ioServices.closeQuietly(fis);
		ioServices.closeQuietly(zis);
		ioServices.deleteQuietly(outFile);
	}

	@Test
	public void checkForRequiredMetadataInTheXml()
			throws Exception {
		FileInputStream fis = new FileInputStream(outFile);
		nonConventionalClosingZipInputStream zis = new nonConventionalClosingZipInputStream(fis);
		ZipEntry ze;
		ze = zis.getNextEntry();
		SAXBuilder builder = new SAXBuilder();
		while (ze != null) {
			List<String> documentIds = this.getDocumentForFolder(records.getFolder_A01().getId());
			//Check if it's the actual xml file.
			if (ze.getName().matches("([A-Za-z0-9\\-\\/\\\\\\(\\)\\ ]+)(\\.xml)") && ze.getName().contains("/")) {
				Document doc = builder.build(zis);
				Element rootElement = (Element) doc.getContent().get(0);
				Element eadheaderElement = rootElement.getChild("eadheader", Namespace.getNamespace("urn:isbn:1-931666-22-9"));
				Element eadid = eadheaderElement.getChild("eadid", Namespace.getNamespace("urn:isbn:1-931666-22-9"));
				assertThat(eadid.getText()).isIn(documentIds);
			}
			ze = zis.getNextEntry();
		}
		ioServices.closeQuietly(zis);
		ioServices.deleteQuietly(outFile);
		zis.doClose();
	}

	@Test
	public void testSIPGenerationWithEmail()
			throws Exception {
		File emailFile = getTestResourceFile("testFile.msg");
		InputStream sha1InputStream = new FileInputStream(emailFile);
		String checksum = DigestUtils.sha1Hex(sha1InputStream);
		ContentVersionDataSummary summary = contentManager.upload(emailFile);
		String emailFileName = "emailTest.msg";
		Email email = rm.newEmail();
		email.setContent(contentManager.createMajor(records.getAdmin(), emailFileName, summary));
		email.setFolder(records.getFolder_A01());
		Transaction transaction = new Transaction();
		transaction.add(email);
		recordServices.execute(transaction);

		SIPBuildAsyncTask task = new SIPBuildAsyncTask("testSIPFile", asList("test1", "test2"),
				Collections.singletonList(email.getId()), Collections.<String>emptyList(), false,
				records.getAdmin().getUsername(), false, getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
		getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(
				new AsyncTaskCreationRequest(task, zeCollection,
						"SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPBuildAsyncTaskAcceptanceTest"));
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
		while (ze != null) {
			if (ze.getName().endsWith(".msg")) {
				assertThat(DigestUtils.sha1Hex(zis)).isEqualTo(checksum);
			}
			if (ze.getName().endsWith(email.getId() + ".xml")) {
				Document doc = builder.build(zis);
				Element rootElement = (Element) doc.getContent().get(0);
				Element archdescElement = rootElement.getChild("archdesc", currentNameSpace);
				Element didElement = archdescElement.getChild("did", currentNameSpace);
				Element unitid = didElement.getChild("unitid", currentNameSpace);
				Element unittitle = didElement.getChild("unittitle", currentNameSpace);
				assertThat(unitid.getText()).isEqualTo(email.getId());
				assertThat(unittitle.getText()).isEqualTo(emailFileName);
			}
			ze = zis.getNextEntry();
		}
		ioServices.closeQuietly(sha1InputStream);
		ioServices.closeQuietly(is);
		ioServices.deleteQuietly(outFile);
		zis.doClose();
	}

	private List<String> getDocumentForFolder(String folderId) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaType type = metadataSchemasManager.getSchemaTypes(zeCollection)
				.getSchemaType(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(type)
				.where(type.getDefaultSchema().getMetadata(com.constellio.app.modules.rm.wrappers.Document.FOLDER))
				.isEqualTo(folderId);
		List<com.constellio.app.modules.rm.wrappers.Document> documents = rm
				.wrapDocuments(searchServices.search(new LogicalSearchQuery(condition)));
		List<String> documentsIds = new ArrayList<>();
		for (com.constellio.app.modules.rm.wrappers.Document document : documents) {
			documentsIds.add(document.getId());
		}
		Folder parentFolder = rm.getFolder(folderId);
		while (parentFolder != null) {
			documentsIds.add(parentFolder.getId());
			try {
				parentFolder = rm.getFolder(parentFolder.getParentFolder());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				parentFolder = null;
			}
		}
		return documentsIds;
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
