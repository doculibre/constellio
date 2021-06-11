package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemaTypeImportServices implements ImportServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaTypeImportServices.class);
	private static final int DEFAULT_BATCH_SIZE = 100;
	private static final String DESCRIPTION = "description";
	public static final String METADATA_LIST = "metadataList";
	private final String collection;
	private final ModelLayerFactory modelLayerFactory;
	private final AppLayerFactory appLayerFactory;
	private int batchSize;
	private int currentElement;
	MetadataSchemasManager metadataSchemasManager;
	ValueListServices valueListServices;

	public SchemaTypeImportServices(AppLayerFactory appLayerFactory, String collection) {
		this(appLayerFactory, appLayerFactory.getModelLayerFactory(), collection, DEFAULT_BATCH_SIZE);
	}

	public SchemaTypeImportServices(AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory,
									String collection,
									int batchSize) {
		this.batchSize = batchSize;
		this.modelLayerFactory = modelLayerFactory;
		this.appLayerFactory = appLayerFactory;
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		valueListServices = new ValueListServices(appLayerFactory, collection);
		this.collection = collection;
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
										BulkImportProgressionListener progressionListener,
										User user, List<String> collections, BulkImportParams params)
			throws RecordsImportServicesRuntimeException {
		return bulkImport(importDataProvider, progressionListener, user, collections);
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider,
										BulkImportProgressionListener progressionListener,
										User user, List<String> collections)
			throws RecordsImportServicesRuntimeException {
		currentElement = 0;
		importDataProvider.initialize();
		BulkImportResults importResults = new BulkImportResults();
		try {
			bulkImport(importResults, importDataProvider, collections);
			return importResults;
		} catch (Exception e) {
			LOGGER.warn(e.toString(), e);
			importResults.add(new ImportError("element" + currentElement, e.getMessage()));
			return importResults;
		} finally {
			importDataProvider.close();
		}
	}

	int bulkImport(BulkImportResults importResults, ImportDataProvider importDataProvider, List<String> collections) {
		int skipped = 0;

		List<String> importedSchemas = importDataProvider.getAvailableSchemaTypes();
		
		for (String importedSchema : importedSchemas) {
			Iterator<ImportData> importDataIterator = importDataProvider.newDataIterator(importedSchema);
			Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, batchSize);
			while (importDataBatches.hasNext()) {
				try {
					List<ImportData> batch = importDataBatches.next();
					skipped += importBatch(importResults, batch);
				} catch (Exception e) {
					skipped++;
					LOGGER.warn(e.toString(), e);
					importResults.add(new ImportError("element" + currentElement + " in schema " + importedSchema + " ",
							e.getMessage()));
				}
			}
		}
		return skipped;
	}

	private int importBatch(BulkImportResults importResults, List<ImportData> batch) {

		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(collection);
		List<Taxonomy> taxonomies = new ArrayList<>();
		int skipped = 0;
		for (ImportData toImport : batch) {
			skipped += importSchemaType(taxonomies, typesBuilder, importResults, toImport);
		}
		try {
			schemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
		for (Taxonomy taxonomy : taxonomies) {
			modelLayerFactory.getTaxonomiesManager().addTaxonomy(taxonomy, schemasManager);
		}

		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (ImportData toImport : batch) {
			currentElement++;
			skipped += importSchemaTypeDisplay(transaction, typesBuilder, importResults, toImport);
		}
		schemasDisplayManager.execute(transaction);

		return skipped;
	}

	private int importSchemaType(List<Taxonomy> taxonomies, MetadataSchemaTypesBuilder typesBuilder,
								 BulkImportResults importResults, ImportData toImport) {
		try {
			String schemaTypeCode_schemaCode = toImport.getLegacyId();
			if (!schemaTypeCode_schemaCode.contains("_")) {
				schemaTypeCode_schemaCode = schemaTypeCode_schemaCode + "_default";
			}
			final String schemaTypeCode = StringUtils.substringBefore(schemaTypeCode_schemaCode, "_");
			final String schemaLocalCode = StringUtils.substringAfter(schemaTypeCode_schemaCode, "_");
			Map<String, Object> fields = new HashMap<>(toImport.getFields());
			final String title = (String) fields.get(DESCRIPTION);
			Map<Language, String> mapLabel = new HashMap<>();
			mapLabel.put(Language.French, title);

			Object metadataListFields = fields.get(METADATA_LIST);
			List<Map<String, String>> metadataListFieldsMap = new ArrayList<>();
			if (metadataListFields != null && metadataListFields instanceof List) {
				metadataListFieldsMap = (List<Map<String, String>>) metadataListFields;
			}
			final List<ImportedMetadata> metadataList;
			if (!metadataListFieldsMap.isEmpty()) {
				metadataList = new ImportedMetadataListBuilder(schemaTypeCode, schemaLocalCode, metadataListFieldsMap, collection)
						.getMetadataList();
			} else {
				metadataList = new ArrayList<>();
			}
			createTaxonomyOrValueDomain(taxonomies, typesBuilder, schemaTypeCode, mapLabel);

			addMetadataList(typesBuilder, schemaTypeCode, schemaLocalCode, title, metadataList);
			return 0;
		} catch (Exception e) {
			addError(e, toImport, importResults);
			return 1;
		}
	}

	private int importSchemaTypeDisplay(SchemaDisplayManagerTransaction transaction,
										MetadataSchemaTypesBuilder typesBuilder,
										BulkImportResults importResults,
										ImportData toImport) {
		try {
			String schemaTypeCode_schemaCode = toImport.getLegacyId();
			if (!schemaTypeCode_schemaCode.contains("_")) {
				schemaTypeCode_schemaCode = schemaTypeCode_schemaCode + "_default";
			}
			final String schemaTypeCode = StringUtils.substringBefore(schemaTypeCode_schemaCode, "_");
			final String schemaLocalCode = StringUtils.substringAfter(schemaTypeCode_schemaCode, "_");
			Map<String, Object> fields = new HashMap<>(toImport.getFields());

			Object metadataListFields = fields.get(METADATA_LIST);
			List<Map<String, String>> metadataListFieldsMap = new ArrayList<>();
			if (metadataListFields != null && metadataListFields instanceof List) {
				metadataListFieldsMap = (List<Map<String, String>>) metadataListFields;
			}
			final List<ImportedMetadata> metadataList;
			if (!metadataListFieldsMap.isEmpty()) {
				metadataList = new ImportedMetadataListBuilder(schemaTypeCode, schemaLocalCode, metadataListFieldsMap, collection)
						.getMetadataList();
			} else {
				metadataList = new ArrayList<>();
			}

			MetadataSchemaTypeBuilder schemaTypeBuilder = getOrCreateSchemaType(typesBuilder, schemaTypeCode);

			MetadataSchemaBuilder schemaBuilder = getOrCreateSchemaBuilder(schemaTypeBuilder, schemaLocalCode);
			List<Metadata> allGlobalMetadata = Schemas.getAllGlobalMetadatas();
			for (ImportedMetadata importedMetadata : metadataList) {

				MetadataBuilder metadata;
				try {

					metadata = schemaBuilder.getMetadata(importedMetadata.getLocalCode());

				} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
					metadata = schemaBuilder.getUserMetadata(importedMetadata.getLocalCode());

				}

				importedMetadata.setCode(metadata.getCode());
				importedMetadata.setLocalCode(metadata.getLocalCode());

				for (Metadata aGlobalMetadata : allGlobalMetadata) {
					if (importedMetadata.getCode().equals(aGlobalMetadata.getCode())) {
						importedMetadata.setGlobal(true);
						break;
					}
				}
			}

			updateMetadataDisplay(transaction, schemaTypeCode_schemaCode, metadataList, typesBuilder);

			return 0;
		} catch (Exception e) {
			addError(e, toImport, importResults);
			return 1;
		}
	}

	private void updateMetadataDisplay(SchemaDisplayManagerTransaction transaction,
									   SchemasDisplayManager schemasDisplayManager,
									   ImportedMetadata importedMetadata, String code) {
		MetadataInputType type = importedMetadata.getInput();

		if (type == null) {
			type = MetadataInputType.FIELD;
		}

		MetadataDisplayConfig metadataDisplayConfig = transaction.getMetadataDisplayConfig(code);

		if (metadataDisplayConfig == null) {
			metadataDisplayConfig = schemasDisplayManager.getMetadata(collection, code);
		}
		metadataDisplayConfig = metadataDisplayConfig.withHighlightStatus(importedMetadata.isHighlight())
				.withVisibleInAdvancedSearchStatus(importedMetadata.isAdvancedSearch()).withInputType(type)
				.withMetadataGroup(importedMetadata.getMetadataGroup());

		transaction.addReplacing(metadataDisplayConfig);
	}

	private void createTaxonomyOrValueDomain(List<Taxonomy> taxonomies, MetadataSchemaTypesBuilder typesBuilder,
											 String typeCode,
											 Map<Language, String> title) {

		try {
			metadataSchemasManager.getSchemaTypes(collection).getSchemaType(typeCode);
		} catch (MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			if (typeCode.startsWith("ddv")) {
				ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(typesBuilder);
				builder.createValueListItemSchema(typeCode, title,
						ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique().titleUnique(false));

			} else if (typeCode.startsWith("taxo") && typeCode.endsWith("Type")) {
				String taxoCode = StringUtils.substringBetween(typeCode, "taxo", "Type");
				if (StringUtils.isBlank(taxoCode)) {
					throw new TaxonomiesManagerRuntimeException.InvalidTaxonomyCode(typeCode);
				}
				taxonomies.add(valueListServices.lazyCreateTaxonomy(typesBuilder, taxoCode, title, true));
			}
		}
	}

	private MetadataSchemaTypeBuilder getOrCreateUserSchemaType(MetadataSchemaTypesBuilder types, String code,
																String title) {
		MetadataSchemaTypeBuilder builder;
		try {
			builder = types.getSchemaType(code);
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
			builder = types.createNewSchemaTypeWithSecurity(code);//"USR" +
		}
		for (String language : appLayerFactory.getCollectionsManager().getCollectionLanguages(collection)) {
			builder.addLabel(Language.withCode(language), title);
		}
		return builder;
	}

	private void addError(Exception e, ImportData toImport, BulkImportResults importResults) {
		LOGGER.warn(e.toString(), e);
		importResults.add(new ImportError(toImport.getLegacyId(), e.getMessage()));
	}

	void addMetadataList(MetadataSchemaTypesBuilder types, String schemaTypeCode, String schemaCode, String schemaLabel,
						 List<ImportedMetadata> importedMetadataList) {
		MetadataSchemaTypeBuilder schemaTypeBuilder = getOrCreateSchemaType(types, schemaTypeCode);

		MetadataSchemaBuilder schemaBuilder = getOrCreateSchemaBuilder(schemaTypeBuilder, schemaCode);
		for (String language : appLayerFactory.getCollectionsManager().getCollectionLanguages(collection)) {
			schemaBuilder.addLabel(Language.withCode(language), schemaLabel);
		}
		List<Metadata> allGlobalMetadata = Schemas.getAllGlobalMetadatas();
		for (ImportedMetadata importedMetadata : importedMetadataList) {
			processMetadata(importedMetadata, schemaBuilder, allGlobalMetadata, types);
		}

		for (ImportedMetadata importedMetadata : importedMetadataList) {
			updateMetadata(importedMetadata, schemaBuilder, types);
		}
	}

	private void updateMetadata(ImportedMetadata importedMetadata, MetadataSchemaBuilder schemaBuilder,
								MetadataSchemaTypesBuilder types) {
		MetadataBuilder builder = getOrCreateMetadataBuilder(schemaBuilder, importedMetadata);
		if (importedMetadata.getUsingReference() != null) {
			MetadataBuilder referenceMetadata = getMetadataBuilder(schemaBuilder, importedMetadata.getUsingReference());
			MetadataSchemaBuilder referencedSchema = types
					.getDefaultSchema(referenceMetadata.getAllowedReferencesBuilder().getSchemaType());
			MetadataBuilder copiedMetadata;
			if (importedMetadata.getCopyMetadata() == null) {
				copiedMetadata = getMetadataBuilder(referencedSchema, importedMetadata.getLocalCode());
			} else {
				copiedMetadata = getMetadataBuilder(referencedSchema, importedMetadata.getCopyMetadata());
			}
			builder.defineDataEntry().asCopied(referenceMetadata, copiedMetadata);
		}
		if (importedMetadata.getCalculator() != null) {
			builder.defineDataEntry().asCalculated(importedMetadata.getCalculator());
		}
	}

	private void processMetadata(ImportedMetadata importedMetadata, MetadataSchemaBuilder schemaBuilder,
								 List<Metadata> allGlobalMetadata, MetadataSchemaTypesBuilder types) {
		MetadataBuilder builder = getOrCreateMetadataBuilder(schemaBuilder, importedMetadata);
		importedMetadata.setCode(builder.getCode());

		updateMetadataSchemaBuilder(types, builder, importedMetadata);

		for (Metadata metadata : allGlobalMetadata) {
			if (importedMetadata.getCode().equals(metadata.getCode())) {
				importedMetadata.setGlobal(true);
				break;
			}
		}
	}

	private MetadataSchemaTypeBuilder getOrCreateSchemaType(MetadataSchemaTypesBuilder types, String schemaTypeCode) {
		try {
			return types.getSchemaType(schemaTypeCode);
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
			return types.createNewSchemaTypeWithSecurity(schemaTypeCode);
		}
	}

	private MetadataSchemaBuilder getOrCreateSchemaBuilder(MetadataSchemaTypeBuilder schemaTypeBuilder,
														   String schemaCode) {
		MetadataSchemaBuilder schemaBuilder;
		try {
			if (schemaCode.isEmpty() || schemaCode.equals("default")) {
				schemaBuilder = schemaTypeBuilder.getDefaultSchema();
			} else {
				schemaBuilder = schemaTypeBuilder.getCustomSchema(schemaCode);
			}
		} catch (MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema e) {
			schemaBuilder = schemaTypeBuilder.createCustomSchema(schemaCode);
		}
		return schemaBuilder;
	}

	private MetadataBuilder getOrCreateMetadataBuilder(
			MetadataSchemaBuilder schemaBuilder, ImportedMetadata importedMetadata) {
		MetadataBuilder builder;
		try {
			builder = schemaBuilder.getMetadata(importedMetadata.getLocalCode());
		} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
			try {
				builder = schemaBuilder.getUserMetadata(importedMetadata.getLocalCode());
			} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e1) {
				builder = schemaBuilder.create("USR" + importedMetadata.getLocalCode());
				importedMetadata.setNewMetadata(true);
			}
		}

		return builder;
	}

	private MetadataBuilder getMetadataBuilder(
			MetadataSchemaBuilder schemaBuilder, String localCode) {
		MetadataBuilder builder;
		try {
			builder = schemaBuilder.getMetadata(localCode);
		} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
			try {
				builder = schemaBuilder.getUserMetadata(localCode);
			} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e1) {
				builder = schemaBuilder.create("USR" + localCode);
			}
		}

		return builder;
	}

	private void updateMetadataSchemaBuilder(MetadataSchemaTypesBuilder types, MetadataBuilder builder,
											 ImportedMetadata importedMetadata) {
		if (importedMetadata.isNewMetadata()) {
			builder.setMultivalue(importedMetadata.isMultivalue());
			builder.setType(importedMetadata.getValueType());
			builder.setSortable(importedMetadata.isSortable());
			builder.setSchemaAutocomplete(importedMetadata.isAutocomplete());
			builder.setSearchable(importedMetadata.isSearchable());
			if (importedMetadata.getValueType().equals(MetadataValueType.REFERENCE)) {
				MetadataSchemaTypeBuilder refBuilder = types.getSchemaType(importedMetadata.getReference());
				Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager()
						.getTaxonomyFor(collection, importedMetadata.getReference());
				if (taxonomy != null) {
					builder.defineTaxonomyRelationshipToType(refBuilder);
				} else {
					builder.defineReferencesTo(refBuilder);
				}
			}
		} else {
			String code = importedMetadata.getCode();
			if (!isInherited(code)) {
				builder.setSortable(importedMetadata.isSortable());
				builder.setSchemaAutocomplete(importedMetadata.isAutocomplete());
				builder.setSearchable(importedMetadata.isSearchable());
			}
		}

		builder.setEnabled(importedMetadata.isEnabled());
		builder.setLabels(importedMetadata.getLabels());
		builder.setDefaultRequirement(importedMetadata.isRequired());
	}

	void updateMetadataDisplay(SchemaDisplayManagerTransaction transaction, String schemaTypeCode_schemaCode,
							   List<ImportedMetadata> importedMetadataList, MetadataSchemaTypesBuilder typesBuilder) {
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		List<String> newMetadataCodes = new ArrayList<>();
		List<String> newMetadataCodesToDisplayInAllSchemas = new ArrayList<>();
		for (ImportedMetadata importedMetadata : importedMetadataList) {
			String code = schemaTypeCode_schemaCode + "_" + importedMetadata.getLocalCode();
			updateMetadataDisplay(transaction, schemasDisplayManager, importedMetadata, code);
			newMetadataCodes.add(importedMetadata.getCode());
			if (importedMetadata.isDisplayInAllSchemas()) {
				newMetadataCodesToDisplayInAllSchemas.add(importedMetadata.getCode());
			}
		}
		addCodesToSchema(transaction, schemaTypeCode_schemaCode, schemasDisplayManager, newMetadataCodes);
		addCodesToAllSchemaDisplay(transaction, schemaTypeCode_schemaCode, schemasDisplayManager,
				newMetadataCodesToDisplayInAllSchemas, typesBuilder);
		saveFacetDisplay(transaction, schemasDisplayManager, importedMetadataList);
	}

	private void addCodesToAllSchemaDisplay(SchemaDisplayManagerTransaction transaction,
											String schemaTypeCode_schemaCode,
											SchemasDisplayManager schemasDisplayManager,
											List<String> newMetadataCodesToDisplayInAllSchemas,
											MetadataSchemaTypesBuilder typesBuilder) {
		if (!newMetadataCodesToDisplayInAllSchemas.isEmpty() && schemaTypeCode_schemaCode.endsWith("_default")) {
			//Add to all schemas
			String schemaTypeCode = StringUtils.substringBeforeLast(schemaTypeCode_schemaCode, "_");
			MetadataSchemaTypeBuilder schemaType = typesBuilder.getSchemaType(schemaTypeCode);
			for (MetadataSchemaBuilder builder : schemaType.getAllSchemas()) {
				String schemaCode = builder.getLocalCode();
				String schemaType_schemaCode = schemaTypeCode + "_" + schemaCode;

				SchemaDisplayConfig schemaConfig = transaction.getModifiedSchema(schemaType_schemaCode);

				if (schemaConfig == null) {
					schemaConfig = schemasDisplayManager.getSchema(collection, schemaType_schemaCode);
				}

				List<String> displayMetadata = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
				List<String> formMetadata = new ArrayList<>(schemaConfig.getFormMetadataCodes());

				displayMetadata.removeAll(newMetadataCodesToDisplayInAllSchemas);
				displayMetadata.addAll(newMetadataCodesToDisplayInAllSchemas);

				schemaConfig = schemaConfig.withDisplayMetadataCodes(displayMetadata);
				schemaConfig = schemaConfig.withFormMetadataCodes(formMetadata);

				transaction.addReplacing(schemaConfig);
			}
		}

	}

	private void addCodesToSchema(SchemaDisplayManagerTransaction transaction, String schemaCode,
								  SchemasDisplayManager schemasDisplayManager, List<String> newCodes) {
		SchemaDisplayConfig schemaConfig = transaction.getModifiedSchema(schemaCode);

		if (schemaConfig == null) {
			schemaConfig = schemasDisplayManager.getSchema(collection, schemaCode);
		}

		List<String> displayMetadata = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		List<String> formMetadata = new ArrayList<>(schemaConfig.getFormMetadataCodes());

		displayMetadata.removeAll(newCodes);
		formMetadata.removeAll(newCodes);
		displayMetadata.addAll(newCodes);
		formMetadata.addAll(newCodes);

		schemaConfig = schemaConfig.withDisplayMetadataCodes(displayMetadata);
		schemaConfig = schemaConfig.withFormMetadataCodes(formMetadata);

		transaction.addReplacing(schemaConfig);
	}

	private void saveFacetDisplay(SchemaDisplayManagerTransaction transaction, SchemasDisplayManager displayManager,
								  List<ImportedMetadata> importedMetadataList) {
		SchemaTypesDisplayConfig typesConfig = transaction.getModifiedCollectionTypes();

		if (typesConfig == null) {
			typesConfig = displayManager.getTypes(collection);
		}
		List<String> facets = new ArrayList<>(typesConfig.getFacetMetadataCodes());
		for (ImportedMetadata importedMetadata : importedMetadataList) {
			if (importedMetadata.isFacet()) {
				if (importedMetadata.isGlobal()) {
					if (!facets.contains(importedMetadata.getLocalCode())) {
						facets.add(importedMetadata.getLocalCode());
					}
				} else {
					if (!facets.contains(importedMetadata.getCode())) {
						facets.add(importedMetadata.getCode());
					}
				}
			} else {
				if (facets.contains(importedMetadata.getLocalCode())) {
					facets.remove(importedMetadata.getLocalCode());
				} else if (facets.contains(importedMetadata.getCode())) {
					facets.remove(importedMetadata.getCode());
				}
			}
		}

		typesConfig = typesConfig.withFacetMetadataCodes(facets);
		transaction.setModifiedCollectionTypes(typesConfig);
	}

	public boolean isInherited(String metadataCode) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		try {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			return metadata.inheritDefaultSchema();
		} catch (MetadataSchemasRuntimeException.NoSuchSchema e) {
			return true;
		}
	}
}
