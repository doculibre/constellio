package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_InvalidRecordId;
import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class SystemStateExportParamsAcceptTest extends ConstellioTest {

	RMTestRecords rmTestRecords = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	String document1Id;
	String document1PreviousContent;
	String document1CurrentContent;

	String document2Id;
	String document2PreviousContent;
	String document2CurrentContent;

	ZipService zipService;

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(HashingEncoding.BASE64_URL_ENCODED);
		givenTransactionLogIsEnabled();
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(rmTestRecords)
						.withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withAllTestUsers().withConstellioRMModule()
		);
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.REWRITE);
		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary resource1 = contentManager.upload(getTestResourceFile("resource1.docx"));
		ContentVersionDataSummary resource2 = contentManager.upload(getTestResourceFile("resource2.docx"));
		ContentVersionDataSummary resource3 = contentManager.upload(getTestResourceFile("resource3.pdf"));
		ContentVersionDataSummary resource4 = contentManager.upload(getTestResourceFile("resource4.pdf"));

		document1PreviousContent = resource1.getHash();
		document1CurrentContent = resource2.getHash();
		document2PreviousContent = resource3.getHash();
		document2CurrentContent = resource4.getHash();

		Document document1 = rm.newDocument().setTitle("Document 1").setFolder(rmTestRecords.folder_A04);
		document1.setContent(contentManager.createMajor(admin, "file1.docx", resource1).updateContent(admin, resource2, true));
		document1Id = document1.getId();

		Document document2 = rm.newDocument().setTitle("Document 2").setFolder(rmTestRecords.folder_A04);
		document2.setContent(contentManager.createMajor(admin, "file2.pdf", resource3).updateContent(admin, resource4, true));
		document2Id = document2.getId();

		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(document1, document2));

		zipService = getModelLayerFactory().getIOServicesFactory().newZipService();
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMove();
	}

	@Test
	public void whenExportingStateWithAllContentThenAllContentVersionsAvailable()
			throws Exception {

		File unzipFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();

		new SystemStateExporter(getAppLayerFactory()).exportSystemToFile(zipFile, params);
		zipService.unzip(zipFile, unzipFolder);

		assertThat(unzipFolder.list()).containsOnly("settings", "content");

		File settingsFolder = new File(unzipFolder, "settings");
		assertThat(settingsFolder.list()).contains("collections.xml", "zeCollection", "anotherCollection");
		assertThat(new File(settingsFolder, "zeCollection").list()).contains("schemas.xml", "roles.xml", "taxonomies.xml");

		File contentFolder = new File(unzipFolder, "content");
		assertThat(contentFolder)
				.has(transactionLogs())
				.has(noTransactionLogBackups())
				.has(contents(document1CurrentContent, document1PreviousContent, document2CurrentContent,
						document2PreviousContent));
	}

	@Test
	public void whenExportingStateWithNoContentThenNoContentVersionsAvailable()
			throws Exception {

		File unzipFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();

		params.setExportNoContent();
		new SystemStateExporter(getAppLayerFactory()).exportSystemToFile(zipFile, params);
		zipService.unzip(zipFile, unzipFolder);

		assertThat(unzipFolder.list()).containsOnly("settings", "content");

		File settingsFolder = new File(unzipFolder, "settings");
		assertThat(settingsFolder.list()).contains("collections.xml", "zeCollection", "anotherCollection");
		assertThat(new File(settingsFolder, "zeCollection").list()).contains("schemas.xml", "roles.xml", "taxonomies.xml");

		File contentFolder = new File(unzipFolder, "content");
		assertThat(contentFolder)
				.has(transactionLogs())
				.has(noTransactionLogBackups())
				.has(noContents());
	}

	@Test
	public void whenExportingStateWithSomeRecordContentThenOnlySpecifiedRecordContent()
			throws Exception {

		File unzipFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();

		params.setOnlyExportContentOfRecords(asList(document1Id));
		new SystemStateExporter(getAppLayerFactory())
				.exportSystemToFile(zipFile, params);
		zipService.unzip(zipFile, unzipFolder);

		assertThat(unzipFolder.list()).containsOnly("settings", "content");

		File settingsFolder = new File(unzipFolder, "settings");
		assertThat(settingsFolder.list()).contains("collections.xml", "zeCollection", "anotherCollection");
		assertThat(new File(settingsFolder, "zeCollection").list()).contains("schemas.xml", "roles.xml", "taxonomies.xml");

		File contentFolder = new File(unzipFolder, "content");
		assertThat(contentFolder)
				.has(transactionLogs())
				.has(noTransactionLogBackups())
				.has(contents(document1CurrentContent, document1PreviousContent));
	}

	@Test(expected = SystemStateExporterRuntimeException_InvalidRecordId.class)
	public void whenExportingStateWithAnInvalidRecordIdThenException()
			throws Exception {

		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();

		params.setOnlyExportContentOfRecords(asList(document1Id, "anInexistingRecord"));
		new SystemStateExporter(getAppLayerFactory()).exportSystemToFile(zipFile, params);
	}

	private Condition<? super File> noContents() {
		return contents();
	}

	private Condition<? super File> contents(String... hash) {
		final List<String> hashes = asList(hash);
		return new Condition<File>() {

			@Override
			public boolean matches(File folder) {

				File resource1 = new File(folder, "F/Fs/Fss/Fss7pKBafi8ok5KaOwEpmNdeGCE=".replace("/", separator));
				File resource1Parsed = new File(folder, "F/Fs/Fss/Fss7pKBafi8ok5KaOwEpmNdeGCE=__parsed".replace("/", separator));

				File resource2 = new File(folder, "T/TI/TIK/TIKwSvHOXHOOtRd1K9t2fm4TQ4I=".replace("/", separator));
				File resource2Parsed = new File(folder, "T/TI/TIK/TIKwSvHOXHOOtRd1K9t2fm4TQ4I=__parsed".replace("/", separator));

				File resource3 = new File(folder, "T/T-/T-4/T-4zq4cGP_tXkdJp_qz1WVWYhoQ=".replace("/", separator));
				File resource3Parsed = new File(folder, "T/T-/T-4/T-4zq4cGP_tXkdJp_qz1WVWYhoQ=__parsed".replace("/", separator));

				File resource4 = new File(folder, "K/KN/KN8/KN8RjbrnBgq1EDDV2U71a6_6gd4=".replace("/", separator));
				File resource4Parsed = new File(folder, "K/KN/KN8/KN8RjbrnBgq1EDDV2U71a6_6gd4=__parsed".replace("/", separator));

				//resource1
				if (hashes.contains(document1PreviousContent)) {
					assertThat(resource1).exists();
					assertThat(resource1Parsed).exists();
				} else {
					assertThat(resource1).doesNotExist();
					assertThat(resource1Parsed).doesNotExist();
				}

				//resource2
				if (hashes.contains(document1CurrentContent)) {
					assertThat(resource2).exists();
					assertThat(resource2Parsed).exists();

				} else {
					assertThat(resource2).doesNotExist();
					assertThat(resource2Parsed).doesNotExist();
				}

				//resource3

				if (hashes.contains(document2PreviousContent)) {
					assertThat(resource3).exists();
					assertThat(resource3Parsed).exists();
				} else {
					assertThat(resource3).doesNotExist();
					assertThat(resource3Parsed).doesNotExist();
				}

				//resource4
				if (hashes.contains(document2CurrentContent)) {
					assertThat(resource4).exists();
					assertThat(resource4Parsed).exists();
				} else {
					assertThat(resource4).doesNotExist();
					assertThat(resource4Parsed).doesNotExist();
				}

				return true;
			}
		};
	}

	private Condition<? super File> transactionLogs() {
		return new Condition<File>() {
			@Override
			public boolean matches(File folder) {
				assertThat(folder.list()).contains("tlogs");
				assertThat(new File(folder, "tlogs").list()).isNotEmpty();
				return true;
			}
		};
	}

	private Condition<? super File> noTransactionLogBackups() {
		return new Condition<File>() {
			@Override
			public boolean matches(File folder) {
				assertThat(folder.list()).doesNotContain("tlogs_bck");
				return true;
			}
		};
	}

}