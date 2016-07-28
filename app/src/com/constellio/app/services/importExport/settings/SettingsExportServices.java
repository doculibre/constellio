package com.constellio.app.services.importExport.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection;
import com.itextpdf.text.pdf.PdfName;

public class SettingsExportServices {

	AppLayerFactory appLayerFactory;
	SystemConfigurationsManager systemConfigurationsManager;
	MetadataSchemasManager schemasManager;
	ValidationErrors validationErrors;

	public SettingsExportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;

		validationErrors = new ValidationErrors();
		systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public ImportedSettings exportSettings(List<String> collections)
			throws ValidationException {

		validate(collections);

		ImportedSettings settings = new ImportedSettings();

		appendSystemConfigurations(settings);

		for (String collection : collections) {
			ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings();
			collectionSettings.setCode(collection);

			// add taxonomies
			addTaxonomies(collection, collectionSettings);

			// add schemaTypes
			MetadataSchemaTypes metadataSchemaTypes = schemasManager.getSchemaTypes(collection);
			for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
				ImportedType importedType = new ImportedType().setCode(type.getCode());

				// add tabs
				List<ImportedTab> tabs = getImportedTabs(collection, type);
				importedType.setTabs(tabs);

				// add default-schema
				MetadataSchema defaultSchema = type.getDefaultSchema();
				ImportedMetadataSchema importedDefaultMetadataSchema = new ImportedMetadataSchema().setCode("default");
				for (Metadata metadata : defaultSchema.getMetadatas()) {
					ImportedMetadata importedMetadata = new ImportedMetadata()
							.setCode(metadata.getCode()).setLabel(metadata.getLabel(Language.French));

					MetadataDisplayConfig displayConfig = appLayerFactory
							.getMetadataSchemasDisplayManager().getMetadata(collection, metadata.getCode());

					importedMetadata.setAdvanceSearchable(displayConfig.isVisibleInAdvancedSearch());

					setImportedDataEntry(metadata, importedMetadata);

					importedMetadata.setDuplicable(metadata.isDuplicable());

					importedMetadata.setType(metadata.getType().name());


					importedDefaultMetadataSchema.addMetadata(importedMetadata);
				}

				importedType.setDefaultSchema(importedDefaultMetadataSchema);
				collectionSettings.addType(importedType);
			}

			settings.addCollectionSettings(collectionSettings);

		}
		return settings;
	}

	private void setImportedDataEntry(Metadata metadata, ImportedMetadata importedMetadata) {
		DataEntry dataEntry = metadata.getDataEntry();

		ImportedDataEntry importedDataEntry = null;
		switch (dataEntry.getType()) {
		case CALCULATED:
			importedDataEntry =
					ImportedDataEntry
							.asCalculated(((CalculatedDataEntry) dataEntry).getCalculator().getClass().getName());
			break;

		case COPIED:
			importedDataEntry.asCopied(((CopiedDataEntry)dataEntry).getReferenceMetadata())
					.withCopiedMetadata((((CopiedDataEntry) dataEntry).getCopiedMetadata()));
			break;

		case SEQUENCE:
			if(StringUtils.isNotBlank(((SequenceDataEntry) dataEntry).getFixedSequenceCode())){
				importedDataEntry = ImportedDataEntry.asFixedSequence(((SequenceDataEntry) dataEntry).getFixedSequenceCode());
			} else if(StringUtils.isNotBlank(((SequenceDataEntry) dataEntry).getMetadataProvidingSequenceCode())){
				importedDataEntry = ImportedDataEntry
						.asMetadataProvidingSequence(((SequenceDataEntry) dataEntry).getMetadataProvidingSequenceCode());
			}
			break;

		// TODO JEXL manquant ?!?

		default:
			break;
		}

		if(importedDataEntry != null) {
			importedMetadata.setDataEntry(importedDataEntry);
		}
	}

	private List<ImportedTab> getImportedTabs(String collection, MetadataSchemaType type) {
		Map<String, Map<Language, String>> metadataGroups =
				appLayerFactory.getMetadataSchemasDisplayManager().getType(collection, type.getCode()).getMetadataGroup();
		List<ImportedTab> tabs = new ArrayList<>();
		for (Map.Entry<String, Map<Language, String>> entry : metadataGroups.entrySet()) {
			ImportedTab importedTab = new ImportedTab()
					.setCode(entry.getKey()).setValue(entry.getValue().get(Language.French));
			tabs.add(importedTab);
		}
		return tabs;
	}

	private void addTaxonomies(String code, ImportedCollectionSettings collectionSettings) {
		ValueListServices valueListServices = new ValueListServices(appLayerFactory, code);
		for (Taxonomy taxonomy : valueListServices.getTaxonomies()) {
			collectionSettings.addTaxonomy(getImportTaxonomyFor(taxonomy));
		}
	}

	private ImportedTaxonomy getImportTaxonomyFor(Taxonomy taxonomy) {
		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy();
		importedTaxonomy.setCode(taxonomy.getCode());
		importedTaxonomy.setTitle(taxonomy.getTitle());

		if (!taxonomy.getGroupIds().isEmpty()) {
			importedTaxonomy.setGroupIds(taxonomy.getGroupIds());
		}

		if (!taxonomy.getUserIds().isEmpty()) {
			importedTaxonomy.setUserIds(taxonomy.getUserIds());
		}

		if (!taxonomy.getSchemaTypes().isEmpty()) {
			importedTaxonomy.setClassifiedTypes(taxonomy.getSchemaTypes());
		}

		importedTaxonomy.setVisibleOnHomePage(taxonomy.isVisibleInHomePage());

		return importedTaxonomy;
	}

	private void appendSystemConfigurations(ImportedSettings settings) {
		for (SystemConfiguration sysConfig : systemConfigurationsManager.getAllConfigurations()) {
			Object value = systemConfigurationsManager.getValue(sysConfig);
			if (value != null && SystemConfigurationType.BINARY != sysConfig.getType()) {
				ImportedConfig importedConfig = new ImportedConfig().setKey(sysConfig.getCode());
				importedConfig.setValue(String.valueOf(value));
				settings.addConfig(importedConfig);
			}
		}
	}

	private void validate(List<String> collections)
			throws ValidationException {

		if (collections == null) {
			throw new RuntimeException("List of collections cannot be null");
		}

		for (String code : collections) {
			try {
				schemasManager.getSchemaTypes(code).getSchemaType(Collection.SCHEMA_TYPE);
			} catch (MetadataSchemasManagerRuntimeException_NoSuchCollection e) {
				Map<String, Object> parameters = new HashMap();
				parameters.put("config", "collection");
				parameters.put("value", code);
				validationErrors.add(SettingsImportServices.class, "collectionNotFound", parameters);
			}
		}

		if (!validationErrors.isEmpty()) {
			throw new ValidationException(validationErrors);
		}
	}
}
