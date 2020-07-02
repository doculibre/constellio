package com.constellio.app.services.importExport.settings;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.appManagement.GetWarVersionUtils;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileReader;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class SettingsExportServicesAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager schemasDisplayManager;
	SettingsExportServices services;
	ImportedSettings settings = new ImportedSettings();
	ImportedCollectionSettings zeCollectionSettings;
	ImportedCollectionSettings anotherCollectionSettings;
	SystemConfigurationsManager systemConfigurationsManager;
	List<String> collections = new ArrayList<>();
	SettingsExportOptions options = new SettingsExportOptions();

	@Test
	public void whenExportingThenGlobalConfigsAreOK()
			throws ValidationException {
		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);
		assertThat(settings).isNotNull();
		assertThat(settings.getConfigs()).extracting("key").contains("requireApprovalForDepositOfSemiActive",
				"deleteDocumentRecordsWithDestruction", "dateFormat", "calculatedInactiveDateNumberOfYearWhenOpenRule",
				"yearEndDate", "calculatedCloseDateNumberOfYearWhenVariableRule", "backupRetentionPeriodInDays",
				"displayDepositedInTrees", "inUpdateProcess", "metadataPopulatePriority", "documentRetentionRules",
				"logoLink", "displaySemiActiveInTrees", "displayContainersInTrees", "PDFACreatedOn",
				"borrowingDurationDays", "enforceCategoryAndRuleRelationshipInFolder", "decommissioningDateBasedOn",
				"trashPurgeDelaiInDays", "calculatedMetadatasBasedOnFirstTimerangePart", "tokenDurationInHours");
	}

	//--------------------------------------------------------------------

	@Test
	public void whenExportingNonExistingCollectionThenError()
			throws ValidationException {

		collections.add("zeMissingCollection");
		assertThatErrorsWhileExportingSettingsExtracting("config", "value").contains(
				tuple("SettingsImportServices_collectionNotFound", "collection", "zeMissingCollection"));
	}

	@Test
	public void givenListCollectionsWhenExportingThenCollectionOK()
			throws ValidationException {
		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);
		assertThat(settings).isNotNull();
		assertThat(settings.getCollectionsSettings()).hasSize(1);
		assertThat(settings.getCollectionsSettings().get(0).getCode()).isEqualTo(zeCollection);
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTaxonomiesOK()
			throws ValidationException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getTaxonomies().size()).isEqualTo(valueListServices.getTaxonomies().size());
		for (ImportedTaxonomy importedTaxonomy : zeCollectionSettings.getTaxonomies()) {
			System.out.println(importedTaxonomy.toString());
		}
	}

	@Test
	public void givenCreatingVersionWhenExportingThenVersionOK()
			throws ValidationException {
		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);
		assertThat(settings).isNotNull();
		assertThat(settings.getImportedSystemVersion()).isNotNull();
		File versionDirectory = new FoldersLocator().getConstellioWebappFolder();
		String version = GetWarVersionUtils.getWarVersion(versionDirectory);
		assertThat(settings.getImportedSystemVersion().getFullVersion()).isEqualTo(version);

		List<InstallableModule> plugins = new ArrayList<>();
		List<String> pluginIds = new ArrayList<>();

		plugins = getAppLayerFactory().getPluginManager().getActivePluginModules();
		pluginIds = plugins.stream().map(plugin -> plugin.getId()).collect(Collectors.toList());
		assertThat(pluginIds).containsAll(settings.getImportedSystemVersion().getPlugins());

	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTypesOK()
			throws ValidationException, IOException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getTypes()).isNotEmpty();
		assertThat(zeCollectionSettings.getTypes().size())
				.isEqualTo(metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaTypes().size());

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTypesTabsOK()
			throws ValidationException, IOException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		Map<String, Map<Language, String>> folderMetadataGroup = getAppLayerFactory()
				.getMetadataSchemasDisplayManager()
				.getType(zeCollection, folderSchemaType.getCode()).getMetadataGroup();

		assertThat(folderMetadataGroup.size()).isEqualTo(importedFolderType.getTabs().size());

		for (Map.Entry<String, Map<Language, String>> tabEntry : folderMetadataGroup.entrySet()) {
			ImportedTab tab = importedFolderType.getTab(tabEntry.getKey());
			assertThat(tab).isNotNull();
			// TODO Valider si on traite seulement le titre en francais
			assertThat(tabEntry.getValue().get(Language.French)).isEqualTo(tab.getValue());
		}

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTypesDefaultSchemaOK()
			throws ValidationException, IOException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		MetadataSchema defaultSchema = folderSchemaType.getDefaultSchema();
		assertThat(defaultSchema).isNotNull();

		List<Metadata> defaultSchemaMetadata = defaultSchema.getMetadatas();
		assertThat(defaultSchemaMetadata).isNotEmpty();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();
		ImportedMetadataSchema importedMetadataSchema = importedFolderType.getDefaultSchema();
		assertThat(importedMetadataSchema).isNotNull();

		List<ImportedMetadata> importedMetadata = importedMetadataSchema.getAllMetadata();
		assertThat(importedMetadata).isNotEmpty();

		assertThat(defaultSchemaMetadata.size()).isEqualTo(importedMetadata.size());

		for (Metadata metadata : defaultSchemaMetadata) {
			assertThat(importedMetadataSchema.getMetadata(metadata.getLocalCode())).isNotNull();
		}

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTypesCustomSchemataOK()
			throws ValidationException, IOException {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRtest1");
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRtest2");
			}
		});

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> customSchemata = folderSchemaType.getAllSchemas();
		assertThat(customSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		List<ImportedMetadataSchema> importedTypeCustomSchemata = importedFolderType.getCustomSchemata();
		assertThat(importedTypeCustomSchemata).extracting("code").containsOnly("USRtest1", "USRtest2");

		for (MetadataSchema customSchema : customSchemata) {
			assertThat(importedFolderType.getSchema(customSchema.getLocalCode())).isNotNull();
		}

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void givenCollectionsListWhenExportingThenFolderMetadataOK()
			throws ValidationException, IOException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> folderSchemata = folderSchemaType.getAllSchemas();
		assertThat(folderSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		for (MetadataSchema metadataSchema : folderSchemata) {
			ImportedMetadataSchema importedMetadataSchema = importedFolderType.getSchema(metadataSchema.getLocalCode());
			assertThat(importedMetadataSchema).isNotNull();

			for (Metadata metadata : metadataSchema.getMetadatas()) {
				ImportedMetadata importedMetadata = importedMetadataSchema.getMetadata(metadata.getLocalCode());
				assertThat(importedMetadata).isNotNull();
				// TODO valider les flags de la métadonnée
			}
		}

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	private ListAssert<Tuple> assertThatErrorsWhileExportingSettingsExtracting(String... parameters)
			throws com.constellio.model.frameworks.validation.ValidationException {

		try {
			services.exportSettings(collections, options);
			fail("ValidationException expected");
			return assertThat(new ArrayList<Tuple>());
		} catch (ValidationException e) {

			return assertThat(extractingSimpleCodeAndParameters(e, parameters));
		}
	}

	@Test
	public void givenAccessRestrictionOnMetadataWhenExportingThenAccessRestrictionAreOK()
			throws ValidationException, IOException {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder metadataSchemaBuilder = types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRtest1");
				MetadataBuilder metadataBuilder = metadataSchemaBuilder.create("USRcustommetadata");
				metadataBuilder.setType(MetadataValueType.BOOLEAN);

				final MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;
				MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(asList("RGI", "ADM"), new ArrayList<String>(),
						new ArrayList<String>(), new ArrayList<String>());

				metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
				metadataBuilder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);
			}
		});

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		zeCollectionSettings.getTypes();

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> customSchemata = folderSchemaType.getAllSchemas();
		assertThat(customSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		List<ImportedMetadataSchema> importedTypeCustomSchemata = importedFolderType.getCustomSchemata();
		assertThat(importedTypeCustomSchemata).extracting("code").containsOnly("USRtest1");


		ImportedMetadata importMetadata = importedFolderType.getSchema("USRtest1").getMetadata("USRcustommetadata");

		assertThat(importMetadata.getRequiredReadRoles()).contains("RGI", "ADM");

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}
	}

	@Test
	public void givenSequencesTablesWhenExportingThenKeyAndValuesExported()
			throws ValidationException, IOException {

		services.sequencesManager.set("sequence1", 42);
		services.sequencesManager.set("sequence2", 666);


		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);
		assertThat(settings.getSequences()).isEmpty();

		options.setExportingSequences(true);
		settings = writeAndReadXML(services.exportSettings(asList(zeCollection), options));
		assertThat(settings.getSequences()).extracting("key", "value")
				.containsOnly(tuple("sequence1", "42"), tuple("sequence2", "666"));

	}

	@Test
	public void givenSystemVersionWhenExportingThenVersionPluginsAndOnlyUSRExported()
			throws ValidationException, IOException {

		options.setOnlyUSR(true);

		ImportedSettings settings = writeAndReadXML(services.exportSettings(asList(zeCollection), options));
		assertThat(settings.getImportedSystemVersion()).isNotNull();
		assertThat(settings.getImportedSystemVersion().getMajorVersion()).isEqualTo(5);
		assertThat(settings.getImportedSystemVersion().getMinorVersion()).isEqualTo(0);
		assertThat(settings.getImportedSystemVersion().getMinorRevisionVersion()).isEqualTo(0);
		assertThat(settings.getImportedSystemVersion().isOnlyUSR()).isTrue();

	}

	private ImportedSettings writeAndReadXML(ImportedSettings exportSettings) throws IOException {

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(exportSettings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		return new SettingsXMLFileReader(document, null, getModelLayerFactory()).read();
	}

	@Test
	public void givenAccessRestrictionOnMetadataOFFolderDefaultSchemaWhenExportingThenAccessRestrictionAreOK()
			throws ValidationException, IOException, ParserConfigurationException, SAXException {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder metadataSchemaBuilder = types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
				MetadataBuilder metadataBuilder = metadataSchemaBuilder.create("custommetadata");
				metadataBuilder.setType(MetadataValueType.BOOLEAN);

				final MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;
				MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(asList("RGI", "ADM"), new ArrayList<String>(),
						new ArrayList<String>(), new ArrayList<String>());

				metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
				metadataBuilder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);
			}
		});

		ImportedSettings settings = services.exportSettings(asList(zeCollection), options);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		zeCollectionSettings.getTypes();

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> customSchemata = folderSchemaType.getAllSchemas();
		assertThat(customSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();


		ImportedMetadata importMetadata = importedFolderType.getDefaultSchema().getMetadata("custommetadata");

		assertThat(importMetadata.getRequiredReadRoles()).contains("RGI", "ADM");

		String outputFilePath = "settings-export-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		Document document = new SettingsXMLFileWriter().writeSettings(settings);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(document, fileOutputStream);
		}

		// Test read wrtie requiredReadRole.
		org.w3c.dom.Document w3cDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(outputFile);

		document = new DOMBuilder().build(w3cDocument);

		SettingsXMLFileReader settingsXMLFileReader = new SettingsXMLFileReader(document, zeCollection, getModelLayerFactory());
		ImportedSettings importedSettings = settingsXMLFileReader.read();
		List<ImportedType> importedTypesList = importedSettings.getCollectionsSettings().get(0).getTypes();

		boolean typeFound = false;

		for (ImportedType importedType : importedTypesList) {
			if (importedType.getCode().equals(Folder.SCHEMA_TYPE)) {
				typeFound = true;
				ImportedMetadata importedMetadata = importedType.getDefaultSchema().getMetadata("custommetadata");
				assertThat(importedMetadata.getRequiredReadRoles().get(0)).isEqualTo("RGI");
				assertThat(importedMetadata.getRequiredReadRoles().get(1)).isEqualTo("ADM");
			}
		}

		assertThat(typeFound).isTrue();

		System.out.println("File Saved!");
	}

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule(),
				withCollection("anotherCollection"));
		services = new SettingsExportServices(getAppLayerFactory());
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		options.setExportingConfigs(true);
	}

	@After
	public void tearDown()
			throws Exception {

	}
}
