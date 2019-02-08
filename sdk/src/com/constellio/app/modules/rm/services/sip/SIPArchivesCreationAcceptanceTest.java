package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.sip.SIPBuilderParams;
import com.constellio.app.services.sip.zip.SIPFileHasher;
import com.constellio.data.dao.services.idGenerator.InMemorySequentialGenerator;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.zipFileWithSameContentExceptingFiles;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SIPArchivesCreationAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;

	@Before
	public void setUp() throws Exception {

		records.copyBuilder = new CopyRetentionRuleBuilder(new InMemorySequentialGenerator());

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		givenTimeIs(new LocalDateTime(2018, 1, 2, 3, 4, 5));

		Transaction tx = new Transaction();

		tx.add(records.getRule1().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule2().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule3().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule4().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule5().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));

		rm.executeTransaction(tx);
	}

	@Test
	public void givenSIPArchivesOfTwoDocumentsInSameFolderThenArchiveContainsAllMetadatasContentsAndManifests()
			throws Exception {

		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip1").listFiles()));


		Transaction tx = new Transaction();
		tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		tx.add(rm.newDocumentWithId("document1").setTitle("Document 1").setFolder("zeFolderId")
				.setContent(majorContent("content1.doc")));

		tx.add(rm.newDocumentWithId("document2").setTitle("Document 2").setFolder("zeFolderId"))
				.setContent(minorContent("content2.doc"));

		rm.executeTransaction(tx);

		File sipFile = buildSIPWithDocuments("document1", "document2");
		System.out.println(sipFile.getAbsolutePath());
		unzipInDownloadFolder(sipFile, "testSIP");

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip1.zip"), "bag-info.txt"));

	}

	@Test
	public void givenSIPArchivesOfAnEmailThenAttachementsExtractedInSIP()
			throws Exception {

		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip2.zip"),
				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip2").listFiles()));


		Transaction tx = new Transaction();
		tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		tx.add(rm.newEmailWithId("theEmailId").setTitle("My important email").setFolder("zeFolderId"))
				.setContent(minorContent("testFile.msg"));


		rm.executeTransaction(tx);

		File sipFile = buildSIPWithDocuments("theEmailId");
		System.out.println(sipFile.getAbsolutePath());
		unzipInDownloadFolder(sipFile, "testSIP");

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip"), "bag-info.txt"));

	}

	private void unzipInDownloadFolder(File sipFile, String name) {
		File folder = new File("/Users/francisbaril/Downloads/" + name);
		try {
			FileUtils.deleteDirectory(folder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		folder.mkdirs();

		try {
			getIOLayerFactory().newZipService().unzip(sipFile, folder);
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}
	}

	//-------------------------------------------


	private File buildSIPWithDocuments(String... documentsIds) throws Exception {

		List<String> bagInfoLines = new ArrayList<>();
		bagInfoLines.add("This is the first bagInfo line");
		bagInfoLines.add("This is the second bagInfo line");
		bagInfoLines.add("This is the last bagInfo line");

		File tempFolder = newTempFolder();

		File sipFile = new File(tempFolder, "test.sip");

		Iterator<Record> recordIterator = getModelLayerFactory().newRecordServices().getRecordsById(zeCollection, asList(documentsIds)).iterator();

		SIPBuilderParams params = new SIPBuilderParams().setProvidedBagInfoHeaderLines(bagInfoLines);
		params.setSipFileHasher(new SIPFileHasher() {
			@Override
			public String computeHash(File input, String sipPath) throws IOException {
				return "CHECKSUM{{" + sipPath.replace("\\", "/ d") + "}}";
			}
		});


		RMSIPBuilder constellioSIP = new RMSIPBuilder(zeCollection, getAppLayerFactory());
		ValidationErrors errors = constellioSIP.buildWithFoldersAndDocuments(sipFile, new ArrayList<String>(), asList(documentsIds), null, params);
		if (!errors.isEmpty()) {
			assertThat(TestUtils.frenchMessages(errors)).describedAs("errors").isEmpty();
		}

		return sipFile;
	}

	private Content majorContent(String filename) throws Exception {
		ContentVersionDataSummary dataSummary =
				getModelLayerFactory().getContentManager().upload(getTestResourceFile(filename));
		return ContentImpl.create("zeContent", users.adminIn(zeCollection), filename, dataSummary, true, false);
	}

	private Content minorContent(String filename) throws Exception {
		ContentVersionDataSummary dataSummary =
				getModelLayerFactory().getContentManager().upload(getTestResourceFile(filename));
		return ContentImpl.create("zeContent", users.adminIn(zeCollection), filename, dataSummary, false, false);
	}
}
