/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.services.importExport.systemStateExport;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_InvalidRecordId;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporterRuntimeException.SystemStateExporterRuntimeException_RecordHasNoContent;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.ConstellioTest;

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
		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(rmTestRecords)
						.withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withAllTestUsers().withConstellioRMModule()
		);

		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.REWRITE);

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary resource1 = contentManager.upload(getTestResourceInputStream("resource1.docx"));
		ContentVersionDataSummary resource2 = contentManager.upload(getTestResourceInputStream("resource2.docx"));
		ContentVersionDataSummary resource3 = contentManager.upload(getTestResourceInputStream("resource3.pdf"));
		ContentVersionDataSummary resource4 = contentManager.upload(getTestResourceInputStream("resource4.pdf"));

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
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();
	}

	@Test
	public void whenExportingStateWithAllContentThenAllContentVersionsAvailable()
			throws Exception {

		File unzipFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();
		new SystemStateExporter(getModelLayerFactory()).exportSystemToFile(zipFile, params);
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
		new SystemStateExporter(getModelLayerFactory()).exportSystemToFile(zipFile, params);
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
		new SystemStateExporter(getModelLayerFactory()).exportSystemToFile(zipFile, params);
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

	@Test(expected = SystemStateExporterRuntimeException_RecordHasNoContent.class)
	public void whenExportingStateWithRecordWithoutContentMetadataThenException()
			throws Exception {

		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();
		params.setOnlyExportContentOfRecords(asList(document1Id, rmTestRecords.filingId_A));
		new SystemStateExporter(getModelLayerFactory()).exportSystemToFile(zipFile, params);
	}

	@Test(expected = SystemStateExporterRuntimeException_InvalidRecordId.class)
	public void whenExportingStateWithAnInvalidRecordIdThenException()
			throws Exception {

		File zipFile = new File(newTempFolder(), "file.zip");
		SystemStateExportParams params = new SystemStateExportParams();
		params.setOnlyExportContentOfRecords(asList(document1Id, "anInexistingRecord"));
		new SystemStateExporter(getModelLayerFactory()).exportSystemToFile(zipFile, params);
	}

	private Condition<? super File> noContents() {
		return contents();
	}

	private Condition<? super File> contents(String... hash) {
		final List<String> hashes = asList(hash);
		return new Condition<File>() {

			@Override
			public boolean matches(File folder) {

				//resource1
				if (hashes.contains(document1PreviousContent)) {
					assertThat(folder.list()).contains("Fs");
					assertThat(new File(folder, "Fs").list())
							.contains("Fss7pKBafi8ok5KaOwEpmNdeGCE=", "Fss7pKBafi8ok5KaOwEpmNdeGCE=__parsed");
				} else {
					assertThat(folder.list()).doesNotContain("Fs");
				}

				//resource2
				if (hashes.contains(document1CurrentContent)) {
					assertThat(folder.list()).contains("TI");
					assertThat(new File(folder, "TI").list())
							.contains("TIKwSvHOXHOOtRd1K9t2fm4TQ4I=", "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=__parsed");
				} else {
					assertThat(folder.list()).doesNotContain("Fs");
				}

				//resource3
				if (hashes.contains(document2PreviousContent)) {
					assertThat(folder.list()).contains("T+4zq4cGP");
					assertThat(new File(folder, "T+4zq4cGP" + File.separator + "tXkdJp").list())
							.contains("qz1WVWYhoQ=", "qz1WVWYhoQ=__parsed");
				} else {
					assertThat(folder.list()).doesNotContain("T+4zq4cGP");
				}

				//resource4
				if (hashes.contains(document2CurrentContent)) {
					assertThat(folder.list()).contains("KN8RjbrnBgq1EDDV2U71a6");
					assertThat(new File(folder, "KN8RjbrnBgq1EDDV2U71a6").list())
							.contains("6gd4=", "6gd4=__parsed");
				} else {
					assertThat(folder.list()).doesNotContain("KN8RjbrnBgq1EDDV2U71a6");
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