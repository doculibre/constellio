package com.constellio.app.services.importExport.settings;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata.ListType;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class SettingsImportServices {

	public static final String CLASSIFIED_IN_GROUP_LABEL = "classifiedInGroupLabel";
	static final String TAXO_PREFIX = "taxo";
	static final String TYPE = "Type";
	static final String TAXO_SUFFIX = TYPE;
	static final String TITLE_FR = "title_fr";
	static final String TITLE_EN = "title_en";
	static final String TAXO = "taxo";
	static final String CONFIG = "config";
	static final String VALUE = "value";
	static final String INVALID_COLLECTION_CODE = "invalidCollectionCode";
	static final String COLLECTION_CODE_NOT_FOUND = "collectionCodeNotFound";
	static final String CODE = "code";
	static final String INVALID_VALUE_LIST_CODE = "InvalidValueListCode";
	static final String EMPTY_TAXONOMY_CODE = "EmptyTaxonomyCode";
	static final String INVALID_TAXONOMY_CODE_PREFIX = "InvalidTaxonomyCodePrefix";
	static final String INVALID_TAXONOMY_CODE_SUFFIX = "InvalidTaxonomyCodeSuffix";
	static final String DDV_PREFIX = "ddvUSR";
	static final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
	static final String CONFIGURATION_NOT_FOUND = "configurationNotFound";
	static final String EMPTY_TYPE_CODE = "emptyTypeCode";
	static final String EMPTY_TAB_CODE = "emptyTabCode";
	static final String NULL_DEFAULT_SCHEMA = "nullDefaultSchema";
	static final String INVALID_SCHEMA_CODE = "invalidSchemaCode";

	AppLayerFactory appLayerFactory;
	SystemConfigurationsManager systemConfigurationsManager;
	MetadataSchemasManager schemasManager;
	ValueListServices valueListServices;

	public SettingsImportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void importSettings(ImportedSettings settings)
			throws ValidationException {

		ValidationErrors validationErrors = new ValidationErrors();
		systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

		validate(settings, validationErrors);

		run(settings);
	}

	private void run(ImportedSettings settings)
			throws ValidationException {

		importGlobalConfigurations(settings);

		for (final ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {

			importCollectionConfigurations(collectionSettings);
		}
	}

	private void importCollectionConfigurations(final ImportedCollectionSettings collectionSettings) {
		final String collectionCode = collectionSettings.getCode();
		final MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collectionCode);

		importCollectionsValueLists(collectionSettings, collectionCode, schemaTypes);

		importCollectionTaxonomies(collectionSettings, collectionCode, schemaTypes);

		importCollectionTypes(collectionSettings, collectionCode, schemaTypes);

	}

	private void importCollectionTypes(final ImportedCollectionSettings settings,
			String collection, final MetadataSchemaTypes schemaTypes) {

		final KeyListMap<String, String> newMetadatas = new KeyListMap<>();

		schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				for (ImportedType importedType : settings.getTypes()) {

					MetadataSchemaTypeBuilder typeBuilder;
					if (!schemaTypes.hasType(importedType.getCode())) {
						typeBuilder = types.createNewSchemaType(importedType.getCode());
					} else {
						typeBuilder = types.getSchemaType(importedType.getCode());
					}

					MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();

					importCustomSchemata(types, importedType.getCustomSchemata(), typeBuilder, newMetadatas);

					importSchemaMetadatas(typeBuilder, importedType.getDefaultSchema(), defaultSchemaBuilder, types,
							newMetadatas);

				}

			}
		});

		MetadataSchemaTypes newSchemaTypes = schemasManager.getSchemaTypes(schemaTypes.getCollection());
		updateSettingsMetadata(settings, newSchemaTypes, newMetadatas);

	}

	private void updateSettingsMetadata(ImportedCollectionSettings settings, MetadataSchemaTypes schemaTypes,
			KeyListMap<String, String> newMetadatas) {

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = new SchemaTypesDisplayTransactionBuilder(schemaTypes,
				displayManager);

		for (ImportedType importedType : settings.getTypes()) {
			setupTypeDisplayConfig(transactionBuilder, importedType, schemaTypes.getSchemaType(importedType.getCode()),
					newMetadatas);
		}

		displayManager.execute(transactionBuilder.build());

	}

	private void setupTypeDisplayConfig(SchemaTypesDisplayTransactionBuilder transactionBuilder,
			ImportedType importedType, MetadataSchemaType type, KeyListMap<String, String> newMetadatas) {

		SchemaTypeDisplayConfig schemaTypeDisplayConfig = transactionBuilder.updateSchemaTypeDisplayConfig(type);
		Map<String, Map<Language, String>> allTabs = new HashMap<>(schemaTypeDisplayConfig.getMetadataGroup());

		Map<String, Map<String, Boolean>> schemaMetasDisplayVisibility, schemaMetasFormVisibility,
				schemaMetasSearchVisibility, schemaMetasTablesVisibility;
		schemaMetasDisplayVisibility = getSchemasMetasVisibility(type, importedType, ListType.DISPLAY, newMetadatas);
		schemaMetasFormVisibility = getSchemasMetasVisibility(type, importedType, ListType.FORM, newMetadatas);
		schemaMetasSearchVisibility = getSchemasMetasVisibility(type, importedType, ListType.SEARCH, newMetadatas);
		schemaMetasTablesVisibility = getSchemasMetasVisibility(type, importedType, ListType.TABLES, newMetadatas);

		for (ImportedMetadata importedMetadata : importedType.getDefaultSchema().getAllMetadata()) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				Metadata metadata = schema.getMetadata(importedMetadata.getCode());
				configureMetadataDisplay(metadata, importedMetadata, transactionBuilder, allTabs);
			}
		}

		for (ImportedMetadataSchema importedMetadataSchema : importedType.getCustomSchemata()) {
			for (ImportedMetadata importedMetadata : importedMetadataSchema.getAllMetadata()) {

				String code = type.getCode() + "_" + importedMetadataSchema.getCode() + "_" + importedMetadata.getCode();
				Metadata metadata = type.getMetadata(code);
				configureMetadataDisplay(metadata, importedMetadata, transactionBuilder, allTabs);
			}
		}

		for (MetadataSchema schema : type.getAllSchemas()) {
			SchemaDisplayConfig schemaDisplayConfig = transactionBuilder.updateSchemaDisplayConfig(schema);

			schemaDisplayConfig = schemaDisplayConfig.withDisplayMetadataCodes(apply(schema.getCode(),
					schemaDisplayConfig.getDisplayMetadataCodes(),
					schemaMetasDisplayVisibility.get(schema.getLocalCode())));

			schemaDisplayConfig = schemaDisplayConfig.withFormMetadataCodes(apply(schema.getCode(),
					schemaDisplayConfig.getFormMetadataCodes(),
					schemaMetasFormVisibility.get(schema.getLocalCode())));

			schemaDisplayConfig = schemaDisplayConfig.withSearchResultsMetadataCodes(apply(schema.getCode(),
					schemaDisplayConfig.getSearchResultsMetadataCodes(),
					schemaMetasSearchVisibility.get(schema.getLocalCode())));

			schemaDisplayConfig = schemaDisplayConfig.withTableMetadataCodes(apply(schema.getCode(),
					schemaDisplayConfig.getTableMetadataCodes(),
					schemaMetasTablesVisibility.get(schema.getLocalCode())));

			transactionBuilder.addReplacing(schemaDisplayConfig);
		}

		transactionBuilder.add(schemaTypeDisplayConfig.withMetadataGroup(allTabs));

	}

	private void configureMetadataDisplay(Metadata metadata, ImportedMetadata importedMetadata,
			SchemaTypesDisplayTransactionBuilder transactionBuilder, Map<String, Map<Language, String>> allTabs) {

		MetadataDisplayConfig displayConfig = transactionBuilder.updateMetadataDisplayConfig(metadata);
		String tab = importedMetadata.getTab();
		if ("default".equals(tab)) {
			tab = "";
		}
		if (StringUtils.isNotBlank(importedMetadata.getTab())) {
			displayConfig = displayConfig.withMetadataGroup(tab);
			if (!allTabs.containsKey(tab) && StringUtils.isNotBlank(tab)) {
				Map<Language, String> labels = new HashMap<>();
				for (Language language : metadata.getLabels().keySet()) {
					labels.put(language, tab);
				}
				allTabs.put(tab, labels);
			}
		}

		transactionBuilder.addReplacing(displayConfig);
	}

	private List<String> apply(String schema, List<String> metadatas, Map<String, Boolean> metasVisibility) {

		if (metasVisibility == null) {
			return metadatas;
		}

		List<String> modifiedMetadatas = new ArrayList<>(metadatas);

		for (Map.Entry<String, Boolean> entry : metasVisibility.entrySet()) {
			String code = schema + "_" + entry.getKey();
			if (Boolean.TRUE == entry.getValue()) {
				if (!modifiedMetadatas.contains(code)) {
					modifiedMetadatas.add(code);
				}

			} else if (Boolean.FALSE == entry.getValue()) {
				modifiedMetadatas.remove(code);
			}
		}

		return modifiedMetadatas;
	}

	private Map<String, Map<String, Boolean>> getSchemasMetasVisibility(MetadataSchemaType type,
			ImportedType importedType, ListType listType, KeyListMap<String, String> newMetadatas) {

		Map<String, Map<String, Boolean>> schemasMetadatas = new HashMap<>();

		for (Map.Entry<String, List<String>> newMetadataInSchemaEntry : newMetadatas.getMapEntries()) {

			if (listType == ListType.DISPLAY || listType == ListType.FORM) {
				if (newMetadataInSchemaEntry.getKey().equals(type.getDefaultSchema().getCode())) {
					for (MetadataSchema schema : type.getAllSchemas()) {
						for (String metadataLocalCode : newMetadataInSchemaEntry.getValue()) {
							Map<String, Boolean> schemaMetadatas = schemasMetadatas.get(schema.getLocalCode());
							if (schemaMetadatas == null) {
								schemaMetadatas = new HashMap<>();
								schemasMetadatas.put(schema.getLocalCode(), schemaMetadatas);
							}
							schemaMetadatas.put(metadataLocalCode, true);
						}
					}
				}

				if (newMetadataInSchemaEntry.getKey().startsWith(type.getCode() + "_")) {
					for (String metadataLocalCode : newMetadataInSchemaEntry.getValue()) {
						Map<String, Boolean> schemaMetadatas = schemasMetadatas.get(newMetadataInSchemaEntry.getKey());
						if (schemaMetadatas == null) {
							schemaMetadatas = new HashMap<>();
							schemasMetadatas.put(newMetadataInSchemaEntry.getKey(), schemaMetadatas);
						}
						schemaMetadatas.put(metadataLocalCode, true);
					}
				}
			}
		}

		for (ImportedMetadata metadata : importedType.getDefaultSchema().getAllMetadata()) {
			Boolean visibleInAllSchemas = metadata.getVisible(listType);
			if (visibleInAllSchemas != null) {
				for (MetadataSchema schema : type.getAllSchemas()) {
					Map<String, Boolean> schemaMetadatas = schemasMetadatas.get(schema.getLocalCode());
					if (schemaMetadatas == null) {
						schemaMetadatas = new HashMap<>();
						schemasMetadatas.put(schema.getLocalCode(), schemaMetadatas);
					}
					schemaMetadatas.put(metadata.getCode(), visibleInAllSchemas);
				}
			} else {
				List<String> visibleInSchemas = metadata.getVisibleInListInSchemas(listType);
				if (visibleInSchemas != null && visibleInSchemas.size() > 0) {
					for (MetadataSchema schema : type.getAllSchemas()) {
						Map<String, Boolean> schemaMetadatas = schemasMetadatas.get(schema.getLocalCode());
						if (schemaMetadatas == null) {
							schemaMetadatas = new HashMap<>();
							schemasMetadatas.put(schema.getLocalCode(), schemaMetadatas);
						}
						schemaMetadatas.put(metadata.getCode(), visibleInSchemas.contains(schema.getLocalCode()));
					}
				}
			}
		}

		for (ImportedMetadataSchema metadataSchema : importedType.getCustomSchemata()) {
			Map<String, Boolean> schemaMetadatas = schemasMetadatas.get(metadataSchema.getCode());
			if (schemaMetadatas == null) {
				schemaMetadatas = new HashMap<>();
				schemasMetadatas.put(metadataSchema.getCode(), schemaMetadatas);
			}
			for (ImportedMetadata metadata : metadataSchema.getAllMetadata()) {
				Boolean visibleInCustomSchemas = metadata.getVisible(listType);
				if (visibleInCustomSchemas != null) {
					schemaMetadatas.put(metadata.getCode(), visibleInCustomSchemas);
				}
			}
		}

		return schemasMetadatas;
	}

	private void importCustomSchemata(MetadataSchemaTypesBuilder types, List<ImportedMetadataSchema> importedMetadataSchemata,
			MetadataSchemaTypeBuilder typeBuilder, KeyListMap<String, String> newMetadatas) {
		for (ImportedMetadataSchema importedMetadataSchema : importedMetadataSchemata) {
			importSchema(types, typeBuilder, importedMetadataSchema, newMetadatas);
		}
	}

	private void importSchema(MetadataSchemaTypesBuilder types,
			MetadataSchemaTypeBuilder typeBuilder, ImportedMetadataSchema importedMetadataSchema,
			KeyListMap<String, String> newMetadatas) {
		MetadataSchemaBuilder customSchemaBuilder;
		try {
			customSchemaBuilder = typeBuilder.createCustomSchema(importedMetadataSchema.getCode(), new HashMap<String, String>());
		} catch (MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined e) {
			customSchemaBuilder = typeBuilder.getCustomSchema(importedMetadataSchema.getCode());
		}
		importSchemaMetadatas(typeBuilder, importedMetadataSchema, customSchemaBuilder, types, newMetadatas);
	}

	private void importSchemaMetadatas(MetadataSchemaTypeBuilder typeBuilder,
			ImportedMetadataSchema importedMetadataSchema, MetadataSchemaBuilder schemaBuilder,
			MetadataSchemaTypesBuilder typesBuilder, KeyListMap<String, String> newMetadatas) {
		for (ImportedMetadata importedMetadata : importedMetadataSchema.getAllMetadata()) {
			createAndAddMetadata(typeBuilder, schemaBuilder, importedMetadata, typesBuilder);
			newMetadatas.add(schemaBuilder.getCode(), importedMetadata.getCode());
		}
	}

	private void createAndAddMetadata(MetadataSchemaTypeBuilder typeBuilder, MetadataSchemaBuilder schemaBuilder,
			ImportedMetadata importedMetadata, MetadataSchemaTypesBuilder typesBuilder) {
		MetadataBuilder metadataBuilder;
		try {
			metadataBuilder = schemaBuilder.create(importedMetadata.getCode());
			MetadataValueType type = EnumUtils.getEnum(MetadataValueType.class, importedMetadata.getType());
			metadataBuilder.setType(type);
		} catch (MetadataSchemaBuilderRuntimeException.MetadataAlreadyExists e) {
			metadataBuilder = schemaBuilder.get(importedMetadata.getCode());
		}

		if (StringUtils.isNotBlank(importedMetadata.getLabel())) {
			Map<Language, String> labels = new HashMap<>();
			labels.put(Language.French, importedMetadata.getLabel());
			metadataBuilder.setLabels(labels);
		}

		if (importedMetadata.getDuplicable() != null) {
			metadataBuilder.setDuplicable(importedMetadata.getDuplicable());
		}

		if (importedMetadata.getEnabled() != null) {
			metadataBuilder.setEnabled(importedMetadata.getEnabled());
		}

		if (importedMetadata.getEncrypted() != null) {
			metadataBuilder.setEncrypted(importedMetadata.getEncrypted());
		}

		if (importedMetadata.getEssential() != null) {
			metadataBuilder.setEssential(importedMetadata.getEssential());
		}

		if (importedMetadata.getEssentialInSummary() != null) {
			metadataBuilder.setEssentialInSummary(importedMetadata.getEssentialInSummary());
		}

		metadataBuilder.setInputMask(importedMetadata.getInputMask());

		if (importedMetadata.getMultiLingual() != null) {
			metadataBuilder.setMultiLingual(importedMetadata.getMultiLingual());
		}

		if (importedMetadata.getMultiValue() != null) {
			metadataBuilder.setMultivalue(importedMetadata.getMultiValue());
		}

		if (importedMetadata.getRecordAutoComplete() != null) {
			metadataBuilder.setSchemaAutocomplete(importedMetadata.getRecordAutoComplete());
		}

		if (importedMetadata.getRequired() != null) {
			metadataBuilder.setDefaultRequirement(importedMetadata.getRequired());
		}

		if (importedMetadata.getSearchable() != null) {
			metadataBuilder.setSearchable(importedMetadata.getSearchable());
		}

		if (importedMetadata.getSortable() != null) {
			metadataBuilder.setSortable(importedMetadata.getSortable());
		}

		if (importedMetadata.getUnique() != null) {
			metadataBuilder.setUniqueValue(importedMetadata.getUnique());
		}

		if (importedMetadata.getUnmodifiable() != null) {
			metadataBuilder.setUnmodifiable(importedMetadata.getUnmodifiable());
		}

		if ("default".equals(schemaBuilder.getCode())) {
			for (String targetSchema : importedMetadata.getEnabledIn()) {
				String prefixedSchemaCode = typeBuilder.getCode() + "_" + targetSchema;
				MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchema(prefixedSchemaCode);
				if (metadataSchemaBuilder != null) {
					MetadataBuilder targetSchemaMetadataBuilder = metadataSchemaBuilder.get(importedMetadata.getCode());
					targetSchemaMetadataBuilder.setEnabled(true);

					if (importedMetadata.getRequiredIn().contains(targetSchema)) {
						targetSchemaMetadataBuilder.setDefaultRequirement(true);
					}
				}
			}
		}
	}

	private void importCollectionTaxonomies(final ImportedCollectionSettings settings,
			final String collectionCode, final MetadataSchemaTypes schemaTypes) {

		final Map<Taxonomy, ImportedTaxonomy> taxonomies = new HashMap<>();
		valueListServices = new ValueListServices(appLayerFactory, collectionCode);

		schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder typesBuilder) {

				for (final ImportedTaxonomy importedTaxonomy : settings.getTaxonomies()) {
					String typeCode = importedTaxonomy.getCode();
					String taxoCode = StringUtils.substringBetween(typeCode, TAXO, TYPE);
					String title = null;
					if (StringUtils.isNotBlank(importedTaxonomy.getTitles().get(TITLE_FR))) {
						title = importedTaxonomy.getTitles().get(TITLE_FR);
					}

					if (!schemaTypes.hasType(importedTaxonomy.getCode())) {
						Taxonomy taxonomy = valueListServices.lazyCreateTaxonomy(typesBuilder, taxoCode, title);

						if (importedTaxonomy.getVisibleOnHomePage() != null) {
							taxonomy = taxonomy.withVisibleInHomeFlag(importedTaxonomy.getVisibleOnHomePage());
						}

						taxonomy = taxonomy.withTitle(importedTaxonomy.getTitles().get(TITLE_FR))
								.withUserIds(importedTaxonomy.getUserIds())
								.withGroupIds(importedTaxonomy.getGroupIds());

						taxonomies.put(taxonomy, importedTaxonomy);

					} else {
						Taxonomy taxonomy = getTaxonomyFor(collectionCode, importedTaxonomy);

						if (StringUtils.isNotBlank(importedTaxonomy.getTitles().get(TITLE_FR))) {
							taxonomy = taxonomy.withTitle(importedTaxonomy.getTitles().get(TITLE_FR));
						}

						if (importedTaxonomy.getVisibleOnHomePage() != null) {
							taxonomy = taxonomy.withVisibleInHomeFlag(importedTaxonomy.getVisibleOnHomePage());
						}

						if (!importedTaxonomy.getGroupIds().isEmpty()) {
							taxonomy = taxonomy.withGroupIds(importedTaxonomy.getGroupIds());
						}

						if (!importedTaxonomy.getUserIds().isEmpty()) {
							taxonomy = taxonomy.withUserIds(importedTaxonomy.getUserIds());
						}

						String groupLabel = $(CLASSIFIED_IN_GROUP_LABEL);

						List<MetadataSchemaType> classifiedTypes = valueListServices.getClassifiedSchemaTypes(taxonomy);
						for (String classifiedType : importedTaxonomy.getClassifiedTypes()) {
							boolean found = false;
							for(MetadataSchemaType type : classifiedTypes) {
								if(classifiedType.equals(type.getCode())){
									found = true;
									break;
								}
							}

							if(!found){
								valueListServices
										.createAMultivalueClassificationMetadataInGroup(taxonomy, classifiedType, groupLabel);
							}
						}

						List<MetadataSchemaType> missing = new ArrayList<>();
						for(MetadataSchemaType type : classifiedTypes) {
							if(!importedTaxonomy.getClassifiedTypes().contains(type.getCode())){
								missing.add(type);
							}
						}

						appLayerFactory.getModelLayerFactory().getTaxonomiesManager().editTaxonomy(taxonomy);
					}
				}
			}
		});

		for (Map.Entry<Taxonomy, ImportedTaxonomy> entry : taxonomies.entrySet()) {
			Taxonomy taxonomy = entry.getKey();
			ImportedTaxonomy importedTaxonomy = entry.getValue();

			appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
					.addTaxonomy(taxonomy, schemasManager);

			String groupLabel = $(CLASSIFIED_IN_GROUP_LABEL);
			for (String classifiedType : importedTaxonomy.getClassifiedTypes()) {
				valueListServices.createAMultivalueClassificationMetadataInGroup(taxonomy, classifiedType, groupLabel);
			}
		}
	}

	private Taxonomy getTaxonomyFor(String collectionCode, ImportedTaxonomy importedTaxonomy) {
		return appLayerFactory.getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(collectionCode, importedTaxonomy.getCode());
	}

	private void importCollectionsValueLists(final ImportedCollectionSettings collectionSettings,
			final String collectionCode, final MetadataSchemaTypes collectionSchemaTypes) {
		schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder schemaTypesBuilder) {

				for (final ImportedValueList importedValueList : collectionSettings.getValueLists()) {

					final String code = importedValueList.getCode();

					String codeModeText = importedValueList.getCodeMode();
					ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode schemaTypeCodeMode =
							ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;

					if (StringUtils.isNotBlank(codeModeText)) {
						schemaTypeCodeMode = EnumUtils
								.getEnum(ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.class, codeModeText);
					}

					if (!collectionSchemaTypes.hasType(code)) {

						ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(schemaTypesBuilder);

						if (importedValueList.getHierarchical() == null || !importedValueList.getHierarchical()) {

							builder.createValueListItemSchema(code,
									importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
						} else {
							builder.createHierarchicalValueListItemSchema(code,
									importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
						}

					} else {
						MetadataSchemaTypeBuilder builder = schemaTypesBuilder.getSchemaType(importedValueList.getCode());

						if (!importedValueList.getTitles().isEmpty()) {
							Map<Language, String> labels = new HashMap<>();
							labels.put(Language.French, importedValueList.getTitles().get(TITLE_FR));
							labels.put(Language.English, importedValueList.getTitles().get(TITLE_EN));
							builder.setLabels(labels);
						}

						if (StringUtils.isNotBlank(codeModeText)) {

							MetadataBuilder metadataBuilder = builder.getDefaultSchema().getMetadata("code");
							if (ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.DISABLED == schemaTypeCodeMode) {
								metadataBuilder.setDefaultRequirement(false);
								metadataBuilder.setEnabled(false);
							} else if (ValueListItemSchemaTypeBuilder
									.ValueListItemSchemaTypeCodeMode.FACULTATIVE
									== schemaTypeCodeMode) {
								metadataBuilder.setDefaultRequirement(false);
								metadataBuilder.setEnabled(false);
								metadataBuilder.setUniqueValue(false);
							} else if (ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE
									== schemaTypeCodeMode) {
								metadataBuilder.setEnabled(true);
								metadataBuilder.setDefaultRequirement(true);
								metadataBuilder.setUniqueValue(true);
							}
						}
					}
				}
			}
		});
	}

	private void importGlobalConfigurations(ImportedSettings settings) {
		for (ImportedConfig importedConfig : settings.getConfigs()) {
			SystemConfiguration config = systemConfigurationsManager.getConfigurationWithCode(importedConfig.getKey());
			if (config != null) {
				if (config.getType() == SystemConfigurationType.BOOLEAN) {
					Object value = Boolean.valueOf(importedConfig.getValue());
					systemConfigurationsManager.setValue(config, value);
				} else if (config.getType() == SystemConfigurationType.INTEGER) {
					int value = Integer.parseInt(importedConfig.getValue());
					systemConfigurationsManager.setValue(config, value);
				} else if (config.getType() == SystemConfigurationType.STRING) {
					systemConfigurationsManager.setValue(config, importedConfig.getValue().trim());
				} else if (config.getType() == SystemConfigurationType.ENUM) {
					Object result = Enum.valueOf((Class<? extends Enum>) config.getEnumClass(), importedConfig.getValue());
					systemConfigurationsManager.setValue(config, result);
				}
			}
		}
	}

	private void validate(ImportedSettings settings, ValidationErrors validationErrors)
			throws ValidationException {

		validateGlobalConfigs(settings, validationErrors);

		validateCollectionConfigs(settings, validationErrors);

		if (!validationErrors.isEmpty()) {
			throw new ValidationException(validationErrors);
		}
	}

	private void validateCollectionConfigs(ImportedSettings settings, ValidationErrors validationErrors) {

		for (ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {

			validateCollectionCode(validationErrors, collectionSettings);

			validateCollectionValueLists(validationErrors, collectionSettings);

			validateCollectionTaxonomies(validationErrors, collectionSettings);

			validateCollectionTypes(validationErrors, collectionSettings);

		}
	}

	private void validateCollectionTypes(ValidationErrors errors, ImportedCollectionSettings settings) {
		for (ImportedType importedType : settings.getTypes()) {

			validateTypeCode(errors, importedType.getCode());

			validateHasDefaultSchema(errors, importedType.getDefaultSchema());

			validateTabs(errors, importedType.getTabs());

			validateCustomSchemas(errors, importedType.getCustomSchemata());

		}
	}

	private void validateCustomSchemas(ValidationErrors errors, List<ImportedMetadataSchema> customSchema) {
		for (ImportedMetadataSchema schema : customSchema) {
			if (StringUtils.isBlank(schema.getCode())) {
				Map<String, Object> parameters = new HashMap();
				parameters.put(CONFIG, CODE);
				parameters.put(VALUE, schema.getCode());
				errors.add(SettingsImportServices.class, INVALID_SCHEMA_CODE, parameters);
			}
		}
	}

	private void validateTabs(ValidationErrors errors, List<ImportedTab> importedTabs) {
		for (ImportedTab tab : importedTabs) {
			if (StringUtils.isBlank(tab.getCode())) {
				Map<String, Object> parameters = new HashMap();
				parameters.put(CONFIG, CODE);
				parameters.put(VALUE, tab.getCode());
				errors.add(SettingsImportServices.class, EMPTY_TAB_CODE, parameters);
			}
		}
	}

	private void validateHasDefaultSchema(ValidationErrors errors, ImportedMetadataSchema defaultSchema) {
		if (defaultSchema == null) {
			Map<String, Object> parameters = new HashMap();
			parameters.put(CONFIG, "default-schema");
			parameters.put(VALUE, null);
			errors.add(SettingsImportServices.class, NULL_DEFAULT_SCHEMA, parameters);
		}
	}

	private void validateTypeCode(ValidationErrors errors, String typeCode) {
		if (StringUtils.isBlank(typeCode)) {
			Map<String, Object> parameters = new HashMap();
			parameters.put(CONFIG, CODE);
			parameters.put(VALUE, typeCode);
			errors.add(SettingsImportServices.class, EMPTY_TYPE_CODE, parameters);
		}
	}

	private void validateCollectionTaxonomies(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
		for (ImportedTaxonomy importedTaxonomy : collectionSettings.getTaxonomies()) {
			validateTaxonomyCode(validationErrors, importedTaxonomy);
		}
	}

	private void validateTaxonomyCode(ValidationErrors validationErrors, ImportedTaxonomy importedTaxonomy) {
		String code = importedTaxonomy.getCode();
		if (StringUtils.isBlank(code)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONFIG, CODE);
			parameters.put(VALUE, importedTaxonomy.getCode());
			validationErrors.add(SettingsImportServices.class,
					EMPTY_TAXONOMY_CODE, parameters);
		} else if (!code.startsWith(TAXO_PREFIX)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONFIG, CODE);
			parameters.put(VALUE, importedTaxonomy.getCode());
			validationErrors.add(SettingsImportServices.class,
					INVALID_TAXONOMY_CODE_PREFIX, parameters);
		} else if (!code.endsWith(TAXO_SUFFIX)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONFIG, CODE);
			parameters.put(VALUE, importedTaxonomy.getCode());
			validationErrors.add(SettingsImportServices.class,
					INVALID_TAXONOMY_CODE_SUFFIX, parameters);
		}
	}

	private void validateCollectionValueLists(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
		for (ImportedValueList importedValueList : collectionSettings.getValueLists()) {
			validateValueListCode(validationErrors, importedValueList);
		}
	}

	private void validateCollectionCode(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
		String collectionCode = collectionSettings.getCode();
		if (StringUtils.isBlank(collectionCode)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONFIG, CODE);
			parameters.put(VALUE, collectionCode);
			validationErrors.add(SettingsImportServices.class,
					INVALID_COLLECTION_CODE, parameters);
		} else {
			try {
				schemasManager.getSchemaTypes(collectionCode);
			} catch (Exception e) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(CONFIG, CODE);
				parameters.put(VALUE, collectionCode);
				validationErrors.add(SettingsImportServices.class,
						COLLECTION_CODE_NOT_FOUND, parameters);
			}
		}
	}

	private void validateValueListCode(ValidationErrors validationErrors, ImportedValueList importedValueList) {
		if (StringUtils.isBlank(importedValueList.getCode()) ||
				!importedValueList.getCode().startsWith(DDV_PREFIX)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(CONFIG, importedValueList.getCode());
			parameters.put(VALUE, importedValueList.getTitles().get(TITLE_FR));
			validationErrors.add(SettingsImportServices.class,
					INVALID_VALUE_LIST_CODE, parameters);
		}
	}

	private void validateGlobalConfigs(ImportedSettings settings, ValidationErrors validationErrors) {
		for (ImportedConfig importedConfig : settings.getConfigs()) {
			SystemConfiguration config = systemConfigurationsManager.getConfigurationWithCode(importedConfig.getKey());
			if (config == null) {
				Map<String, Object> parameters = toParametersMap(importedConfig);
				validationErrors.add(SettingsImportServices.class, CONFIGURATION_NOT_FOUND, parameters);
			} else if (importedConfig.getValue() == null) {
				Map<String, Object> parameters = toParametersMap(importedConfig);
				validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
			} else {
				if (config.getType() == SystemConfigurationType.BOOLEAN) {
					validateBooleanValueConfig(validationErrors, importedConfig);
				} else if (config.getType() == SystemConfigurationType.INTEGER) {
					validateIntegerValueConfig(validationErrors, importedConfig);
				} else if (config.getType() == SystemConfigurationType.STRING) {
					validateStringValueConfig(validationErrors, importedConfig);
				}
			}
		}
	}

	private void validateBooleanValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
		if (!asList("true", "false").contains(String.valueOf(importedConfig.getValue()))) {
			Map<String, Object> parameters = toParametersMap(importedConfig);
			validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
		}
	}

	private void validateIntegerValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
		try {
			Integer.parseInt(importedConfig.getValue());
		} catch (NumberFormatException e) {
			Map<String, Object> parameters = toParametersMap(importedConfig);
			validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
		}
	}

	private void validateStringValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
		if (importedConfig.getValue() == null) {
			Map<String, Object> parameters = toParametersMap(importedConfig);
			validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
		}
	}

	private Map<String, Object> toParametersMap(ImportedConfig importedConfig) {
		Map<String, Object> parameters = new HashMap();
		parameters.put(CONFIG, importedConfig.getKey());
		parameters.put(VALUE, importedConfig.getValue());
		return parameters;
	}

}
