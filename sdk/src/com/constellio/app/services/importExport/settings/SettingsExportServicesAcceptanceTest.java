package com.constellio.app.services.importExport.settings;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;

public class SettingsExportServicesAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager schemasDisplayManager;
	SettingsExportServices services;
	ImportedSettings settings = new ImportedSettings();
	ImportedCollectionSettings zeCollectionSettings;
	ImportedCollectionSettings anotherCollectionSettings;
	SystemConfigurationsManager systemConfigurationsManager;
	List<String> collections = new ArrayList<>();

	@Test
	public void whenExportingThenGlobalConfigsAreOK()
			throws ValidationException {
		ImportedSettings settings = services.exportSettings(asList(zeCollection));
		assertThat(settings).isNotNull();
		assertThat(settings.getConfigs()).isNotEmpty().hasSize(62);
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
		ImportedSettings settings = services.exportSettings(asList(zeCollection));
		assertThat(settings).isNotNull();
		assertThat(settings.getCollectionsSettings()).hasSize(1);
		assertThat(settings.getCollectionsSettings().get(0).getCode()).isEqualTo(zeCollection);
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTaxonomiesOK()
			throws ValidationException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getTaxonomies().size()).isEqualTo(valueListServices.getTaxonomies().size());
		for (ImportedTaxonomy importedTaxonomy : zeCollectionSettings.getTaxonomies()) {
			System.out.println(importedTaxonomy.toString());
		}
	}

	@Test
	public void givenCollectionsListWhenExportingThenCollectionTypesOK()
			throws ValidationException, IOException {

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

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

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

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

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

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
			assertThat(importedMetadataSchema.getMetadata(metadata.getCode())).isNotNull();
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

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> customSchemata = folderSchemaType.getAllSchemas();
		assertThat(customSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		List<ImportedMetadataSchema> importedTypeCustomSchemata = importedFolderType.getCustomSchemata();
		assertThat(importedTypeCustomSchemata).isNotEmpty();

		assertThat(customSchemata.size()).isEqualTo(importedTypeCustomSchemata.size());

		for (MetadataSchema customSchema : customSchemata) {
			assertThat(importedFolderType.getSchema(customSchema.getCode())).isNotNull();
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

		ImportedSettings settings = services.exportSettings(asList(zeCollection));

		ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsSettings().get(0);

		MetadataSchemaType folderSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(folderSchemaType).isNotNull();

		List<MetadataSchema> folderSchemata = folderSchemaType.getAllSchemas();
		assertThat(folderSchemata).isNotNull();

		ImportedType importedFolderType = zeCollectionSettings.getType("folder");
		assertThat(importedFolderType).isNotNull();

		for (MetadataSchema metadataSchema : folderSchemata) {
			ImportedMetadataSchema importedMetadataSchema = importedFolderType.getSchema(metadataSchema.getCode());
			assertThat(importedMetadataSchema).isNotNull();

			for (Metadata metadata : metadataSchema.getMetadatas()) {
				ImportedMetadata importedMetadata = importedMetadataSchema.getMetadata(metadata.getCode());
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
			services.exportSettings(collections);
			fail("ValidationException expected");
			return assertThat(new ArrayList<Tuple>());
		} catch (ValidationException e) {

			return assertThat(extractingSimpleCodeAndParameters(e, parameters));
		}
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
	}

	@After
	public void tearDown()
			throws Exception {

	}
}
