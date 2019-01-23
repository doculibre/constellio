package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.intelligid.ConstellioSIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Content;
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
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SIPArchivesCreationAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		givenTimeIs(new LocalDateTime(2018, 1, 2, 3, 4, 5));
	}

	@Test
	public void givenSIPArchivesOfTwoDocumentsInSameFolderThenArchiveContainsAllMetadatasContentsAndManifests()
			throws Exception {

		Transaction tx = new Transaction();
		Folder zeFolder = tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1)).setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));
		Document document1 = tx.add(rm.newDocumentWithId("document1").setTitle("Document 1").setFolder("zeFolderId")
				.setContent(majorContent("content1.doc")));
		Document document2 = tx.add(rm.newDocumentWithId("document2").setTitle("Document 2").setFolder("zeFolderId"))
				.setContent(minorContent("content2.doc"));
		rm.executeTransaction(tx);

		File sipFile1 = buildSIPWithDocuments("document1", "document2");
		File sipFile2 = buildSIPWithDocuments("document1", "document2");

		File testSIPFolder1 = new File("/Users/francisbaril/Downloads/testSIP1");
		File testSIPFolder2 = new File("/Users/francisbaril/Downloads/testSIP2");

		FileUtils.deleteDirectory(testSIPFolder1);
		FileUtils.deleteDirectory(testSIPFolder2);
		testSIPFolder1.mkdirs();
		testSIPFolder1.mkdirs();

		getIOLayerFactory().newZipService().unzip(sipFile1, testSIPFolder1);
		getIOLayerFactory().newZipService().unzip(sipFile2, testSIPFolder2);

		assertThat(sipFile1.length()).isEqualTo(sipFile2.length());
	}

	//-------------------------------------------


	private File buildSIPWithDocuments(String... documentsIds) throws Exception {

		List<String> bagInfoLines = new ArrayList<>();
		bagInfoLines.add("This is the first bagInfo line");
		bagInfoLines.add("This is the second bagInfo line");
		bagInfoLines.add("This is the last bagInfo line");

		File sipFile = new File(newTempFolder(), "test.sip");
		SIPFilter filter = new SIPFilter(zeCollection, getAppLayerFactory())
				.withIncludeDocumentIds(asList(documentsIds));
		ConstellioSIPObjectsProvider metsObjectsProvider = new ConstellioSIPObjectsProvider(zeCollection, getAppLayerFactory(),
				filter, new ProgressInfo());
		ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, bagInfoLines, false,
				getAppLayerFactory().newApplicationService().getWarVersion(), new ProgressInfo(), Locale.FRENCH) {
			@Override
			protected String getHash(File file, String sipPath) throws IOException {
				return "{{" + sipPath.replace("\\", "/ d") + "}}";
			}
		};
		ValidationErrors errors = constellioSIP.build(sipFile);
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
