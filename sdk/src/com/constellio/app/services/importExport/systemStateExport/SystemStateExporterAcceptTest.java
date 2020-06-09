package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemStateExporterAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void whenExportingSavestatesUsingGenereatedBaseFileThenOk() throws Exception {

		givenTransactionLogIsEnabled();

		//First (and maybe more) tlog file(s) for system preparation records
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();
		getDataLayerFactory().getSequencesManager().set("aNiceSequence", 42);

		//Second tlog file is containing the dark lord
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("darthvader").setTitle("darthvader"));
		getModelLayerFactory().newRecordServices().flushRecords();
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();

		getAppLayerFactory().getAppLayerBackgroundThreadsManager().getCreateBaseSaveStateBackgroundAction().run();

		//Third tlog file is containing the dog
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("nemo").setTitle("nemo"));
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();

		//Fourth tlog file is containing the dark lord
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("PrivateRyan").setTitle("PrivateRyan"));

		File exportToFolder = newTempFolder();
		new SystemStateExporter(getAppLayerFactory()).exportSystemToFolder(exportToFolder,
				new SystemStateExportParams().setUseWeeklyExport(true));

		File tlogs = new File(exportToFolder, "content" + File.separator + "tlogs");

		//Tlogs has only 3 files (1 base file and 2 files for following transactions)
		assertThat(tlogs.listFiles()).hasSize(3);
		assertThatThingsAppearInOnlyOneFile(tlogs);

	}

	@Test
	public void whenExportingSavestatesWithoutUsingGenereatedBaseFileThenDoNotUseIt() throws Exception {

		givenTransactionLogIsEnabled();

		//First (and maybe more) tlog file(s) for system preparation records
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();
		getDataLayerFactory().getSequencesManager().set("aNiceSequence", 42);

		//Second tlog file is containing the dark lord
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("darthvader").setTitle("darthvader"));
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();

		getAppLayerFactory().getAppLayerBackgroundThreadsManager().getCreateBaseSaveStateBackgroundAction().run();

		//Third tlog file is containing the dog
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("nemo").setTitle("nemo"));
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();

		//Fourth tlog file is containing the dark lord
		getModelLayerFactory().newRecordServices().add(rm.newFolderType().setCode("PrivateRyan").setTitle("PrivateRyan"));

		File exportToFolder = newTempFolder();
		new SystemStateExporter(getAppLayerFactory()).exportSystemToFolder(exportToFolder,
				new SystemStateExportParams().setUseWeeklyExport(false));

		File tlogs = new File(exportToFolder, "content" + File.separator + "tlogs");

		//The number of transaction is impredictable, but it will more that 3 for sure
		assertThat(tlogs.listFiles().length).isGreaterThan(3);

		assertThatThingsAppearInOnlyOneFile(tlogs);
	}

	@Test
	public void whenExportingPartialSaveStateIncludingARecordWithContentThenAllContentVersionsIncluded()
			throws Exception {
		givenTransactionLogIsEnabled();
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		//First (and maybe more) tlog file(s) for system preparation records
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.update(records.getDocumentWithContent_A19().setTitle("PrivateRyanDocument"));
		getDataLayerFactory().getSecondTransactionLogManager().regroupAndMoveInVault();
		getDataLayerFactory().getSequencesManager().set("aNiceSequence", 42);

		String exportedId = records.getDocumentWithContent_A19().getId();

		System.out.println(records.getDocumentWithContent_A19().getContent().getCurrentVersion().getHash());

		File exportToFolder = newTempFolder();
		new SystemStateExporter(getAppLayerFactory()).exportSystemToFolder(exportToFolder,
				new SystemStateExportParams().setOnlyExportContentOfRecords(Arrays.asList(exportedId))
						.setUseWeeklyExport(false));

		File content = new File(exportToFolder, "content");
		File tlogs = new File(content, "tlogs");

		//The number of transaction is impredictable, but it will more that 3 for sure
		//		assertThat(tlogs.listFiles().length).isEqualTo(1);

		//assertThatThingsAppearInOnlyOneFileInPartialSavestate(tlogs);
		String relativePath = "E/EZ/EZT/EZTT43G5FWATODGNNIQF6YKXWWXHJLUT".replace("/", File.separator);
		assertThat(new File(content, relativePath)).exists();
		assertThat(new File(content, relativePath + "__parsed")).exists();

	}

	private void assertThatThingsAppearInOnlyOneFile(File tlogs) throws IOException {
		int occurenceOfDarthVador = 0;
		int occurenceOfNemo = 0;
		int occurenceOfPrivateRyan = 0;
		int occurenceOfNiceSequence = 0;
		int occurenceOfKey = 0;
		int occurenceOfRadis = 0;

		for (File tlogFile : tlogs.listFiles()) {
			String content = FileUtils.readFileToString(tlogFile, "UTF-8");
			if (content.contains("darthvader")) {
				occurenceOfDarthVador++;
			}
			if (content.contains("nemo")) {
				occurenceOfNemo++;
			}
			if (content.contains("PrivateRyan")) {
				occurenceOfPrivateRyan++;
			}
			if (content.contains("the_private_key")) {
				occurenceOfKey++;
			}
			if (content.contains("aNiceSequence")) {
				occurenceOfNiceSequence++;
			}
			if (content.contains("Radis")) {
				occurenceOfRadis++;
			}
		}

		assertThat(occurenceOfDarthVador).isEqualTo(1);
		assertThat(occurenceOfNemo).isEqualTo(1);
		assertThat(occurenceOfPrivateRyan).isEqualTo(1);
		assertThat(occurenceOfNiceSequence).isEqualTo(1);
		assertThat(occurenceOfKey).isEqualTo(1);
		assertThat(occurenceOfRadis).isEqualTo(1);
	}

	private void assertThatThingsAppearInOnlyOneFileInPartialSavestate(File tlogs) throws IOException {
		int occurenceOfPrivateRyan = 0;
		int occurenceOfNiceSequence = 0;
		int occurenceOfKey = 0;
		int occurenceOfRadis = 0;

		for (File tlogFile : tlogs.listFiles()) {
			String content = FileUtils.readFileToString(tlogFile, "UTF-8");
			if (content.contains("darthvader")) {
				if (content.contains("PrivateRyanDocument")) {
					occurenceOfPrivateRyan++;
				}
				if (content.contains("the_private_key")) {
					occurenceOfKey++;
				}
				if (content.contains("aNiceSequence")) {
					occurenceOfNiceSequence++;
				}
				if (content.contains("Radis")) {
					occurenceOfRadis++;
				}
			}

			assertThat(occurenceOfPrivateRyan).isEqualTo(1);
			assertThat(occurenceOfNiceSequence).isEqualTo(1);
			assertThat(occurenceOfKey).isEqualTo(1);
			assertThat(occurenceOfRadis).isEqualTo(1);
		}
	}

}
