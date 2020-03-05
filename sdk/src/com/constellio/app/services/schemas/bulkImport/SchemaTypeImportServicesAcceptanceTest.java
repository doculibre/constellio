package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportSchemaTypesDataProvider;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTypeImportServicesAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	SchemaTypeImportServices importServices;
	SearchServices searchServices;
	UserServices userServices;
	SchemasDisplayManager schemasDisplayManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		progressionListener = new LoggerBulkImportProgressionListener();
		importServices = new SchemaTypeImportServices(getAppLayerFactory(), getModelLayerFactory(), zeCollection, 100);
		searchServices = getModelLayerFactory().newSearchServices();
		userServices = getModelLayerFactory().newUserServices();
		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenImportingDDVXMLFileThenImportedCorrectly()
			throws Exception {
		File usersFile = getTestResourceFile("valueDomain.xml");
		importDDVAndValidate(
				XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile, usersFile.getName()));
	}

	@Test
	public void whenImportingDocumentXMLFileThenImportedCorrectly()
			throws Exception {
		File usersFile = getTestResourceFile("documentSchemas.xml");
		importDocumentAndValidate(
				XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile, usersFile.getName()));
	}

	@Test
	public void whenImportingDocumentWithMetadataReferencesThenImportedCorrectly()
			throws Exception {
		File ddvFile = getTestResourceFile("valueDomain.xml");
		importDDV(ddvFile);
		File documentFile = getTestResourceFile("documentSchemasWithReferences.xml");
		importDocumentWithRefAndValidate(
				XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), documentFile, documentFile.getName()));
	}

	@Test
	public void whenImportingDocumentSchemaWithMetadataCopyingTheValueDomainTitleThenImportedCorrectly()
			throws Exception {
		File ddvFile = getTestResourceFile("valueDomain.xml");
		importDDV(ddvFile);
		File documentFile = getTestResourceFile("documentSchemasWithCopiedReference.xml");

		importInZeCollection(
				XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), documentFile, documentFile.getName()));

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema schema = types.getSchema(Document.DEFAULT_SCHEMA);
		Metadata metadata = schema.getMetadata("USRcopiedTaxoDomaineTitle");
		assertThat(metadata.getDataEntry().getType()).isEqualTo(DataEntryType.COPIED);

		CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();
		assertThat(dataEntry.getCopiedMetadata()).isEqualTo("taxoDomaineHierarchiqueType_default_title");
		assertThat(dataEntry.getReferenceMetadata()).isEqualTo("document_default_USRreferenceToTaxoDomaineHierarchiqueType");

		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(zeCollection,
				"document_default_USRcopiedTaxoDomaineTitle");
		assertThat(metadataDisplayConfig.isVisibleInAdvancedSearch()).isTrue();
		assertThat(metadata.isSearchable()).isTrue();

		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, "document_default");
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("document_default_USRcopiedTaxoDomaineTitle");
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).doesNotContain("document_default_USRcopiedTaxoDomaineTitle");
	}

	@Test
	public void whenImportingDocumentSchemaWithCalculatedMetadataThenImportedCorrectly()
			throws Exception {
		File documentFile = getTestResourceFile("documentSchemasWithCalculatedMetadata.xml");

		importInZeCollection(
				XMLImportSchemaTypesDataProvider.forSingleXMLFile(getModelLayerFactory(), documentFile, documentFile.getName()));

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema schema = types.getSchema(Document.DEFAULT_SCHEMA);

		Metadata calculatedMetadata = schema.getMetadata("USRcalculatedMD");
		assertThat(calculatedMetadata.getDataEntry().getType()).isEqualTo(DataEntryType.CALCULATED);
		assertThat(calculatedMetadata.isSearchable()).isFalse();
		CalculatedDataEntry calculatedDataEntry = ((CalculatedDataEntry) calculatedMetadata.getDataEntry());
		MetadataValueCalculator<?> calculator = calculatedDataEntry.getCalculator();
		DummyCalculator expectedCalculator = new DummyCalculator();
		assertThat(calculator.getReturnType()).isEqualTo(expectedCalculator.getReturnType());
		assertThat(calculator.getDefaultValue()).isEqualTo(expectedCalculator.getDefaultValue());
		assertThat(calculator.getDependencies()).isEqualTo(expectedCalculator.getDependencies());
		assertThat(calculator.isMultiValue()).isEqualTo(expectedCalculator.isMultiValue());
		assertThat(calculator.calculate(null)).isEqualTo(expectedCalculator.calculate(null));
		CalculatorParameters params = new CalculatorParameters(new HashMap<Dependency, Object>(), "zeRecordId", null,
				types.getSchemaType(Document.SCHEMA_TYPE), zeCollection, false, calculatedMetadata);
		assertThat(calculator.calculate(params)).isEqualTo(expectedCalculator.calculate(params));

		SchemaDisplayConfig schemaDisplayConfigPapier = schemasDisplayManager.getSchema(zeCollection, "document_papier");
		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, "document_default");
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("document_default_USRcalculatedMD");
		assertThat(schemaDisplayConfigPapier.getDisplayMetadataCodes()).doesNotContain("document_default_USRcalculatedMD");

		Metadata calculatedMetadata2 = schema.getMetadata("USRcalculatedMD");
		assertThat(calculatedMetadata2.getDataEntry().getType()).isEqualTo(DataEntryType.CALCULATED);
		assertThat(calculatedMetadata2.isSearchable()).isFalse();

		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("document_default_USRcalculatedMDDisplayedInSchemas");
		assertThat(schemaDisplayConfigPapier.getDisplayMetadataCodes())
				.contains("document_default_USRcalculatedMDDisplayedInSchemas");
	}

	private void importInZeCollection(XMLImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		assertThat(results.getImportErrors()).isEmpty();
	}

	private void importDocumentWithRefAndValidate(XMLImportDataProvider importDataProvider) {
		importInZeCollection(importDataProvider);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType schemaType = types.getSchemaType("document");
		validateRefSchemaImport(schemaType);
	}

	private void validateRefSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getSchema("ref").getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRmdRefFolder")) {
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.REFERENCE);
				assertThat(metadata.getLabel(Language.French)).isEqualTo("ref folder");
				assertThat(metadata.isDefaultRequirement()).isTrue();
				assertThat(metadata.isEnabled()).isTrue();
				assertThat(metadata.isTaxonomyRelationship()).isFalse();
				assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("folder");
			} else if (metadata.getLocalCode().equals("USRmdRefTaxo")) {
				assertThat(metadata.isTaxonomyRelationship()).isTrue();
				assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo("taxoDomaineHierarchiqueType");
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRmdRefFolder");
		assertThat(metadataLocalCodes).contains("USRmdRefTaxo");
	}

	private void importDDV(File ddvFile) {
		XMLImportDataProvider importDataProvider = XMLImportSchemaTypesDataProvider
				.forSingleXMLFile(getModelLayerFactory(), ddvFile, ddvFile.getName());
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors()).hasSize(0);
	}

	private void importDocumentAndValidate(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors().size()).isEqualTo(0);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType schemaType = types.getSchemaType("document");
		assertThat(schemaType).isNotNull();

		validateDefaultSchemaImport(schemaType);
		validatePapierSchemaImport(schemaType);
	}

	private void validatePapierSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getSchema("papier").getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRcat1")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel(Language.French)).isEqualTo("cat 1");
				assertThat(metadata.isDefaultRequirement()).isFalse();
				assertThat(metadata.isEnabled()).isFalse();
			} else if (metadata.getLocalCode().equals("USRmd2Papier")) {
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel(Language.French)).isEqualTo("md2 papier");
				assertThat(metadata.isDefaultRequirement()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRcat1");
		assertThat(metadataLocalCodes).contains("USRmd2Papier");
	}

	private void validateDefaultSchemaImport(MetadataSchemaType schemaType) {
		MetadataList metadataList = schemaType.getDefaultSchema().getMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRcat1")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				Map<Language, String> labels = metadata.getLabels();
				assertThat(metadata.getLabel(Language.French)).isEqualTo("cat 1");
				assertThat(metadata.isDefaultRequirement()).isFalse();
				assertThat(metadata.isEnabled()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).contains("USRcat1");

	}

	private void importDDVAndValidate(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		for (ImportError importError : results.getImportErrors()) {
			System.out.println(importError.getCompleteErrorMessage());
		}
		assertThat(results.getImportErrors()).hasSize(0);
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		validateDDVImport(types);
		validateTaxonomyImport(types);
		validateDDVWithMetadata(types);
	}

	private void validateDDVWithMetadata(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("ddvMasqueSaisieLocalisation");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel(Language.French)).isEqualTo("Masque saisie");
		MetadataList metadataList = schemaType.getAllMetadatas();
		ListIterator<Metadata> it = metadataList.listIterator();
		List<String> metadataLocalCodes = new ArrayList<>();
		while (it.hasNext()) {
			Metadata metadata = it.next();
			if (metadata.getLocalCode().equals("USRregex")) {
				//<element code="regex" label="Regex" required="true" type="string"/>
				assertThat(metadata.getType()).isEqualTo(MetadataValueType.STRING);
				assertThat(metadata.getLabel(Language.French)).isEqualTo("Regex");
				assertThat(metadata.isDefaultRequirement()).isTrue();
			}
			metadataLocalCodes.add((metadata.getLocalCode()));
		}
		assertThat(metadataLocalCodes).containsAll(Arrays.asList(new String[]{"USRregex"}));
	}

	private void validateDDVImport(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("ddvCategoriesOrganisations");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel(Language.French)).isEqualTo("Catégories d'organisations");
	}

	private void validateTaxonomyImport(MetadataSchemaTypes types) {
		MetadataSchemaType schemaType = types.getSchemaType("taxoDomaineHierarchiqueType");
		assertThat(schemaType).isNotNull();
		assertThat(schemaType.getLabel(Language.French)).isEqualTo("Nouvelle taxo");
		MetadataList metadataList = schemaType.getAllMetadatas();
		List<String> taxoDomaineHierarchiqueTypeLocalCodes = new ArrayList<>();
		ListIterator<Metadata> it = metadataList.listIterator();
		while (it.hasNext()) {
			taxoDomaineHierarchiqueTypeLocalCodes.add((it.next().getLocalCode()));
		}
		assertThat(taxoDomaineHierarchiqueTypeLocalCodes).containsAll(Arrays.asList(new String[]{"USRmd1Taxo", "USRmd2Taxo"}));
		Taxonomy taxonomy = getModelLayerFactory().getTaxonomiesManager()
				.getTaxonomyFor(zeCollection, "taxoDomaineHierarchiqueType");
		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getSchemaTypes()).containsOnly(schemaType.getCode());
	}
}
