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
package com.constellio.app.services.schemas.bulkImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException;

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
	final private String defaultTaxonomyGroup;
	MetadataSchemasManager metadataSchemasManager;
	ValueListServices valueListServices;

	public SchemaTypeImportServices(AppLayerFactory appLayerFactory, String collection) {
		this(appLayerFactory, appLayerFactory.getModelLayerFactory(), collection, DEFAULT_BATCH_SIZE);
	}

	public SchemaTypeImportServices(AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory, String collection,
			int batchSize) {
		this.batchSize = batchSize;
		this.modelLayerFactory = modelLayerFactory;
		this.appLayerFactory = appLayerFactory;
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		valueListServices = new ValueListServices(appLayerFactory, collection);
		this.collection = collection;
		defaultTaxonomyGroup = i18n.$("AddEditTaxonomyView.classifiedObject.folder");
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider, BulkImportProgressionListener progressionListener,
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
					skipped += importBatch(importResults, batch, collections, importedSchema);
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

	private int importBatch(BulkImportResults importResults, List<ImportData> batch, List<String> collections,
			String importedSchema) {
		int skipped = 0;
		for (ImportData toImport : batch) {
			currentElement++;
			skipped += importSchemaType(importResults, toImport, collections);
		}
		return skipped;
	}

	private int importSchemaType(BulkImportResults importResults, ImportData toImport, List<String> collections) {
		try {
			String schemaTypeCode_schemaCode = toImport.getLegacyId();
			if (!schemaTypeCode_schemaCode.contains("_")) {
				schemaTypeCode_schemaCode = schemaTypeCode_schemaCode + "_default";
			}
			final String schemaTypeCode = StringUtils.substringBefore(schemaTypeCode_schemaCode, "_");
			final String schemaLocalCode = StringUtils.substringAfter(schemaTypeCode_schemaCode, "_");
			Map<String, Object> fields = new HashMap<>(toImport.getFields());
			final String title = (String) fields.get(DESCRIPTION);

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
			createTaxonomyOrValueDomain(schemaTypeCode, title);

			MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					addMetadataList(types, schemaTypeCode, schemaLocalCode, title, metadataList);
				}
			});
			updateMetadataDisplay(schemaTypeCode_schemaCode, metadataList);
			return 0;
		} catch (Exception e) {
			addError(e, toImport, importResults);
			return 1;
		}
	}

	private void updateMetadataDisplay(SchemasDisplayManager schemasDisplayManager, ImportedMetadata importedMetadata) {
		MetadataInputType type = importedMetadata.getInput();

		if (type == null) {
			type = MetadataInputType.FIELD;
		}

		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(collection, importedMetadata.getCode());
		if (metadataDisplayConfig == null) {
			metadataDisplayConfig = new MetadataDisplayConfig(collection, importedMetadata.getCode(),
					importedMetadata.isAdvancedSearch(),
					type, importedMetadata.isHighlight(), importedMetadata.getMetadataGroup());
		} else {
			metadataDisplayConfig = metadataDisplayConfig.withHighlightStatus(importedMetadata.isHighlight())
					.withVisibleInAdvancedSearchStatus(importedMetadata.isAdvancedSearch()).withInputType(type)
					.withMetadataGroup(importedMetadata.getMetadataGroup());
		}

		schemasDisplayManager.saveMetadata(metadataDisplayConfig);
	}

	private void createTaxonomyOrValueDomain(String typeCode, String title) {
		try {
			metadataSchemasManager.getSchemaTypes(collection).getSchemaType(typeCode);
		} catch (MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			if (typeCode.startsWith("ddv")) {
				valueListServices.createValueDomain(typeCode, title);
			} else if (typeCode.startsWith("taxo") && typeCode.endsWith("Type")) {
				String taxoCode = StringUtils.substringBetween(typeCode, "taxo", "Type");
				if (StringUtils.isBlank(taxoCode)) {
					throw new TaxonomiesManagerRuntimeException.InvalidTaxonomyCode(typeCode);
				}
				valueListServices.createTaxonomy(taxoCode, title);
			}
		}
	}

	private MetadataSchemaTypeBuilder getOrCreateUserSchemaType(MetadataSchemaTypesBuilder types, String code, String title) {
		MetadataSchemaTypeBuilder builder;
		try {
			builder = types.getSchemaType(code);
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
			builder = types.createNewSchemaType(code);//"USR" +
		}
		builder.setLabel(title);
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
		schemaBuilder.setLabel(schemaLabel);
		List<Metadata> allGlobalMetadata = Schemas.getAllGlobalMetadatas();
		for (ImportedMetadata importedMetadata : importedMetadataList) {
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
	}

	private MetadataSchemaTypeBuilder getOrCreateSchemaType(MetadataSchemaTypesBuilder types, String schemaTypeCode) {
		try {
			return types.getSchemaType(schemaTypeCode);
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
			return types.createNewSchemaType(schemaTypeCode);
		}
	}

	private MetadataSchemaBuilder getOrCreateSchemaBuilder(MetadataSchemaTypeBuilder schemaTypeBuilder, String schemaCode) {
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

	private MetadataBuilder getOrCreateMetadataBuilder(MetadataSchemaBuilder schemaBuilder, ImportedMetadata importedMetadata) {
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
		builder.setLabel(importedMetadata.getLabel());
		builder.setDefaultRequirement(importedMetadata.isRequired());
	}

	void updateMetadataDisplay(String schemaCode, List<ImportedMetadata> importedMetadataList) {
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		List<String> newMetadataCodes = new ArrayList<>();
		for (ImportedMetadata importedMetadata : importedMetadataList) {
			updateMetadataDisplay(schemasDisplayManager, importedMetadata);
			if (importedMetadata.isNewMetadata()) {
				newMetadataCodes.add(importedMetadata.getCode());
			}
		}
		saveSchemaDisplay(schemaCode, schemasDisplayManager, newMetadataCodes);
		saveFacetDisplay(schemasDisplayManager, importedMetadataList);
	}

	private void saveSchemaDisplay(String schemaCode, SchemasDisplayManager schemasDisplayManager, List<String> newCodes) {
		SchemaDisplayConfig schemaConfig = schemasDisplayManager.getSchema(collection, schemaCode);
		List<String> displayMetadata = new ArrayList<>(schemaConfig.getDisplayMetadataCodes());
		List<String> formMetadata = new ArrayList<>(schemaConfig.getFormMetadataCodes());
		List<String> searchMetadata = new ArrayList<>(schemaConfig.getSearchResultsMetadataCodes());

		displayMetadata.addAll(newCodes);
		formMetadata.addAll(newCodes);
		searchMetadata.addAll(newCodes);

		schemaConfig = schemaConfig.withDisplayMetadataCodes(displayMetadata);
		schemaConfig = schemaConfig.withFormMetadataCodes(formMetadata);

		schemasDisplayManager.saveSchema(schemaConfig);
	}

	private void saveFacetDisplay(SchemasDisplayManager displayManager, List<ImportedMetadata> importedMetadataList) {
		SchemaTypesDisplayConfig typesConfig = displayManager.getTypes(collection);
		List<String> facets = new ArrayList<>(typesConfig.getFacetMetadataCodes());
		for (ImportedMetadata importedMetadata : importedMetadataList) {
			if (importedMetadata.isFacet()) {
				if (importedMetadata.isGlobal()) {
					if (!facets.contains(importedMetadata.getLocalCode()))
						facets.add(importedMetadata.getLocalCode());
				} else {
					if (!facets.contains(importedMetadata.getCode()))
						facets.add(importedMetadata.getCode());
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
		displayManager.saveTypes(typesConfig);
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
