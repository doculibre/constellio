package com.constellio.app.services.migrations;

import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.AppLayerConfigurationAlteration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.SolrSDKToolsServices;
import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ComboMigrationsAcceptanceTest extends ConstellioTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	// Step 1
	@Test
	public void validateCoreMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
			}
		});

	}

	// Step 2
	@Test
	public void validateCoreTasksMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withTaskModule();
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
			}
		});

	}

	// Step 3
	@Test
	public void validateCoreRMMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withConstellioRMModule();
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetSchema(zeCollection, Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
				getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(false);
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetMetadata(zeCollection, "containerRecord_default_administrativeUnit");
			}
		});

	}

	// Step 4
	@Test
	public void validateCoreRobotsMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withRobotsModule();
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
			}
		});

	}

	// Step 5
	@Test
	public void validateCoreESMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withConstellioESModule();
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
			}
		});
	}

	@Test
	public void validateCoreESRMMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withConstellioESModule().withConstellioRMModule();
				getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getMetadata("userTask_default_status").setDefaultValue(null);
					}
				});
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
				getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(false);
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetMetadata(zeCollection, "containerRecord_default_administrativeUnit");
			}
		});

	}

	@Test
	public void validateCoreRMESMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withConstellioRMModule().withConstellioESModule();
				getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getMetadata("userTask_default_status").setDefaultValue(null);
					}
				});
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
				getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(false);
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetMetadata(zeCollection, "containerRecord_default_administrativeUnit");

				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "printable_report");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_reactivationRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_returnRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_borrowRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetSchema(zeCollection, "userTask_borrowExtensionRequest");

			}
		});

	}

	@Test
	public void validateCoreESRMRobotsMigrationHighway()
			throws Exception {

		validate(new SetupScript() {
			@Override
			public void setupCollection() {
				givenCollection(zeCollection).withMockedAvailableModules(false).withConstellioRMModule().withConstellioESModule()
						.withRobotsModule();
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection,
						Authorization.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserFolder.DEFAULT_SCHEMA);
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, UserDocument.DEFAULT_SCHEMA);
				getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(false);
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetMetadata(zeCollection, "containerRecord_default_administrativeUnit");

				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetSchema(zeCollection, "printable_report");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_reactivationRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_returnRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager().resetSchema(zeCollection, "userTask_borrowRequest");
				getAppLayerFactory().getMetadataSchemasDisplayManager()
						.resetSchema(zeCollection, "userTask_borrowExtensionRequest");
			}
		});

	}

	//	@Test
	//	public void validateCoreTasksRobotsRMESMigrationHighway()
	//			throws Exception {
	//
	//		validate(new SetupScript() {
	//			@Override
	//			public void setupCollection() {
	//				givenCollection(zeCollection).withMockedAvailableModules(false).withTaskModule().withRobotsModule()
	//						.withConstellioRMModule()
	//						.withConstellioESModule();
	//			}
	//		});
	//
	//	}

	//	@Test
	//	public void validateCoreESMigrationHighway()
	//			throws Exception {
	//
	//		validate(new SetupScript() {
	//			@Override
	//			public void setupCollection() {
	//				givenCollection(zeCollection).withConstellioESModule();
	//			}
	//		});
	//
	//	}
	//
	//	@Test
	//	public void validateCoreRMESMigrationHighway()
	//			throws Exception {
	//
	//		validate(new SetupScript() {
	//			@Override
	//			public void setupCollection() {
	//				givenCollection(zeCollection).withConstellioRMModule().withConstellioESModule();
	//			}
	//		});
	//
	//	}
	//
	//	@Test
	//	public void validateCoreRMESRobotsMigrationHighway()
	//			throws Exception {
	//
	//		validate(new SetupScript() {
	//			@Override
	//			public void setupCollection() {
	//				givenCollection(zeCollection).withConstellioRMModule().withConstellioESModule().withRobotsModule();
	//			}
	//		});
	//
	//	}

	public void validate(SetupScript setupScript)
			throws Exception {

		cacheIntegrityCheckedAfterTest = false;

		System.out.println("\n\n---------- 1 ----------");
		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				configuration.setUniqueKeyToBeCreated("123-456-789");
			}
		});

		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(true);
			}
		});

		LocalDateTime time = new LocalDateTime();
		givenTimeIs(time);
		setupScript.setupCollection();

		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify("_system_", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.resetAllIds();
			}
		});
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.resetAllIds();
			}
		});

		getAppLayerFactory().getMetadataSchemasDisplayManager().rewriteInOrderAndGetCodes(zeCollection);
		getAppLayerFactory().getMetadataSchemasDisplayManager().rewriteInOrderAndGetCodes(Collection.SYSTEM_COLLECTION);
		DataLayerFactory dataLayerFactory = getAppLayerFactory().getModelLayerFactory().getDataLayerFactory();
		String nextSequence1 = dataLayerFactory.getUniqueIdGenerator().next();
		String nextSecondarySequence1 = dataLayerFactory.getSecondaryUniqueIdGenerator().next();
		File settingsFolder = getDataLayerFactory().getDataLayerConfiguration().getSettingsFileSystemBaseFolder();
		File settings1 = temporaryFolder.newFolder("settings1");
		FileUtils.copyDirectory(settingsFolder, settings1);
		SolrSDKToolsServices tools = new SolrSDKToolsServices(getDataLayerFactory().newRecordDao());
		tools.flushAndDeleteContentMarkers();
		VaultSnapshot snapshot1 = tools.snapshot();

		resetTestSession();
		cacheIntegrityCheckedAfterTest = false;

		System.out.println("\n\n---------- 2 ----------");

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				configuration.setUniqueKeyToBeCreated("123-456-789");
			}
		});

		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenTimeIs(time);
		setupScript.setupCollection();
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify("_system_", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.resetAllIds();
			}
		});
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.resetAllIds();
			}
		});
		getAppLayerFactory().getMetadataSchemasDisplayManager().rewriteInOrderAndGetCodes(zeCollection);
		getAppLayerFactory().getMetadataSchemasDisplayManager().rewriteInOrderAndGetCodes(Collection.SYSTEM_COLLECTION);
		dataLayerFactory = getAppLayerFactory().getModelLayerFactory().getDataLayerFactory();
		String nextSequence2 = dataLayerFactory.getUniqueIdGenerator().next();
		String nextSecondarySequence2 = dataLayerFactory.getSecondaryUniqueIdGenerator().next();
		settingsFolder = getDataLayerFactory().getDataLayerConfiguration().getSettingsFileSystemBaseFolder();
		File settings2 = temporaryFolder.newFolder("settings2");
		FileUtils.copyDirectory(settingsFolder, settings2);
		tools = new SolrSDKToolsServices(getDataLayerFactory().newRecordDao());
		tools.flushAndDeleteContentMarkers();
		VaultSnapshot snapshot2 = tools.snapshot();

		compareFolder(settings1, settings2);
		tools.ensureSameSnapshots("", snapshot1, snapshot2);
		assertThat(nextSequence1).isEqualTo(nextSequence2);
		assertThat(nextSecondarySequence1).isEqualTo(nextSecondarySequence2);
	}

	private String contentExceptVersion(File file) {
		try {
			List<String> lines = new ArrayList<>(FileUtils.readLines(file));
			if (file.getName().endsWith(".xml")) {
				lines.remove(0);

				if (lines.size() > 0) {
					lines.remove(0);
				}
			}
			return StringUtils.join(lines, "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void compareFolder(File folderOfSettings1, File folderOfSettings2) {

		List<String> filesInFolder1 = getFilesInFolder(folderOfSettings1);
		List<String> filesInFolder2 = getFilesInFolder(folderOfSettings2);
		String folderAbsolutePath = "/settings/" + StringUtils.substringAfter(folderOfSettings1.getAbsolutePath(), "/settings1/");
		assertThat(filesInFolder1).describedAs("Folder '" + folderAbsolutePath + "'").isEqualTo(filesInFolder2);

		for (String file : filesInFolder1) {
			File file1 = new File(folderOfSettings1, file);
			File file2 = new File(folderOfSettings2, file);
			String fileAbsolutePath = folderAbsolutePath + file;
			//if (!file1.getName().contains("schemasDisplay.xml")) {

			String contentOfFileMigratedUsingComboScript = contentExceptVersion(file1);
			String contentOfFileMigratedUsingAtomicScripts = contentExceptVersion(file2);

			if (file.endsWith(".properties")) {
				List<String> linesOfComboMigrationFile = asList(contentOfFileMigratedUsingComboScript.split("\n"));
				Collections.sort(linesOfComboMigrationFile);
				List<String> linesOfMigrationFile = asList(contentOfFileMigratedUsingAtomicScripts.split("\n"));
				Collections.sort(linesOfMigrationFile);

				assertThat(linesOfComboMigrationFile).describedAs("Content of file '" + fileAbsolutePath + "")
						.isEqualTo(linesOfMigrationFile);

			} else {

				if (!contentOfFileMigratedUsingComboScript.equals(contentOfFileMigratedUsingAtomicScripts)) {
					assertThat("FICHIER GÉNÉRÉ AVEC LES WOMBO COMBO (ne pas tenir compte de cette ligne) :\n"
							   + contentOfFileMigratedUsingComboScript).describedAs("Actual content of file '" + fileAbsolutePath
																					+ "' generated using combo script is not equal to the content generated using atomic scripts")
							.isEqualTo("FICHIER GÉNÉRÉ SANS LES WOMBO COMBO (ne pas tenir compte de cette ligne) :\n"
									   + contentOfFileMigratedUsingAtomicScripts);
				}
			}
			System.out.println(file1.getName() + " is OK");
			//}
		}

		List<String> foldersInFolder1 = getFoldersInFolder(folderOfSettings1);
		List<String> foldersInFolder2 = getFoldersInFolder(folderOfSettings2);
		assertThat(foldersInFolder1).describedAs("Folder '" + folderAbsolutePath + "'")
				.isEqualTo(foldersInFolder2);

		for (String file : foldersInFolder1) {
			File folder1 = new File(folderOfSettings1, file);
			File folder2 = new File(folderOfSettings2, file);

			compareFolder(folder1, folder2);
		}
	}

	public interface SetupScript {

		void setupCollection() throws Exception;

	}

	private static List<String> getFilesInFolder(File folder) {
		String[] filenames = folder.list(onlyFile);
		if (filenames == null) {
			return new ArrayList<>();
		}
		List<String> files = new ArrayList<>(asList(filenames));
		Collections.sort(files);
		return files;
	}

	private static List<String> getFoldersInFolder(File folder) {
		String[] filenames = folder.list(onlyFolder);
		if (filenames == null) {
			return new ArrayList<>();
		}
		List<String> files = new ArrayList<>(asList(filenames));
		Collections.sort(files);
		return files;
	}

	private static FilenameFilter onlyFile = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isFile();
		}
	};
	private static FilenameFilter onlyFolder = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	};
}
