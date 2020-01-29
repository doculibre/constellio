package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

import java.io.Serializable;
import java.util.*;

import static com.constellio.app.services.schemasDisplay.SchemaDisplayUtils.getCustomSchemaDefaultDisplay;
import static com.constellio.app.services.schemasDisplay.SchemaDisplayUtils.getDefaultSchemaDefaultDisplay;
import static com.constellio.app.ui.i18n.i18n.$;

public class SchemasDisplayManagerCache implements Serializable {

	private String collection;
	private SchemaTypesDisplayConfig collectionTypes;
	private Map<String, SchemaTypeDisplayConfig> types = new HashMap<>();
	private Map<String, SchemaDisplayConfig> schemas = new HashMap<>();
	private Map<String, MetadataDisplayConfig> metadatas = new HashMap<>();
	private Set<String> returnedFields;
	List<Language> languages;

	public SchemasDisplayManagerCache(String collection, List<Language> languages) {
		this.collection = collection;
		this.languages = languages;
	}

	void set(SchemaTypesDisplayConfig types) {
		collectionTypes = types;
	}

	void set(SchemaTypeDisplayConfig type) {
		types.put(type.getSchemaType(), type);
	}

	void setSchemaTypeDisplayConfigs(Map<String, SchemaTypeDisplayConfig> map) {
		types = map;
	}

	void set(SchemaDisplayConfig schema) {
		schemas.put(schema.getSchemaCode(), schema);
	}

	void setSchemaDisplayConfigs(Map<String, SchemaDisplayConfig> map) {
		schemas = map;
	}

	void set(MetadataDisplayConfig metadata) {
		metadatas.put(metadata.getMetadataCode(), metadata);
	}

	void setMetadataDisplayConfigs(Map<String, MetadataDisplayConfig> map) {
		metadatas = map;
	}

	public SchemaTypesDisplayConfig getTypes() {
		if (collectionTypes == null) {
			collectionTypes = new SchemaTypesDisplayConfig(collection);
			// No need to write the xml, just save the default config in the
			// cache to reuse it
			set(collectionTypes);
		}
		return collectionTypes;
	}

	private SchemaTypesDisplayConfig getDefaultSchemaTypesDisplayConfig() {
		return new SchemaTypesDisplayConfig(collection);
	}

	public SchemaTypeDisplayConfig getType(String typeCode) {
		SchemaTypeDisplayConfig config = types.get(typeCode);
		if (config == null) {
			config = getDefaultSchemaTypeDisplayConfig(typeCode);
			// No need to write the xml, just save the default config in the
			// cache to reuse it
			set(config);
		}
		return config;
	}

	private SchemaTypeDisplayConfig getDefaultSchemaTypeDisplayConfig(String typeCode) {

		Map<String, Map<Language, String>> metadataGroup = new LinkedHashMap<>();
		Map<Language, String> label = new HashMap<>();
		// TODO iterate on all collections' languages

		for (Language language : languages) {
			label.put(language, $("default", language.getLocale()));
		}
		metadataGroup.put("default", label);
		return new SchemaTypeDisplayConfig(collection, typeCode, metadataGroup);
	}

	public SchemaDisplayConfig getSchema(String schemaCode, MetadataSchemasManager metadataSchemasManager) {
		SchemaDisplayConfig config = schemas.get(schemaCode);
		if (config == null) {
			config = getSchemaDefaultDisplay(schemaCode, metadataSchemasManager);
			// No need to write the xml, just save the default config in the
			// cache to reuse it
			set(config);
		}
		return config;
	}

	public SchemaDisplayConfig getSchemaDefaultDisplay(String schemaCode,
													   MetadataSchemasManager metadataSchemasManager) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		if (schemaCode.endsWith("_default")) {
			return getDefaultSchemaDefaultDisplay(schemaCode, types);
		} else {

			String schemaType = new SchemaUtils().getSchemaTypeCode(schemaCode);
			String defaultSchema = schemaType + "_default";
			SchemaDisplayConfig schema = getSchema(defaultSchema, metadataSchemasManager);
			return getCustomSchemaDefaultDisplay(schema, schemaCode, types);
		}
	}

	public MetadataDisplayConfig getMetadata(String metadataCode, MetadataSchemasManager metadataSchemasManager) {
		MetadataDisplayConfig config = metadatas.get(metadataCode);
		if (config == null) {

			config = getDefaultMetadata(metadataCode, metadataSchemasManager);

			// No need to write the xml, just save the default config in the
			// cache to reuse it
			set(config);
		}
		return config;
	}

	public Set<String> getReturnedFieldsForSearch(MetadataSchemasManager metadataSchemasManager) {
		return getReturnedFieldsFor(metadataSchemasManager, "search");
	}

	public Set<String> getReturnedFieldsForTable(MetadataSchemasManager metadataSchemasManager) {
		return getReturnedFieldsFor(metadataSchemasManager, "table");
	}

	private Set<String> getReturnedFieldsFor(MetadataSchemasManager metadataSchemasManager, String viewMode) {
		if (returnedFields == null) {

			Set<String> dataStoreCodes = new HashSet<>();
			dataStoreCodes.add(Schemas.TITLE.getDataStoreCode());
			for (MetadataSchemaType type : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {
				for (Metadata metadata : type.getAllMetadatas()) {
					if (metadata.isEssentialInSummary()) {
						dataStoreCodes.add(metadata.getDataStoreCode());
					}
				}

				for (MetadataSchema schema : type.getAllSchemas()) {
					SchemaDisplayConfig schemaDisplayConfig = getSchema(schema.getCode(), metadataSchemasManager);
					List<String> metadatas = new ArrayList<>();
					if ("search".equals(viewMode)) {
						metadatas = schemaDisplayConfig.getSearchResultsMetadataCodes();
					} else if ("table".equals(viewMode)) {
						metadatas = schemaDisplayConfig.getTableMetadataCodes();
					}
					for (String displayedMetadata : metadatas) {
						if (schema.hasMetadataWithCode(displayedMetadata)) {
							Metadata metadata = schema.getMetadata(displayedMetadata);
							dataStoreCodes.add(metadata.getDataStoreCode());
						}
					}
				}
			}
			returnedFields = dataStoreCodes;
		}
		return returnedFields;
	}

	private MetadataDisplayConfig getDefaultMetadata(String metadataCode,
													 MetadataSchemasManager metadataSchemasManager) {
		MetadataDisplayConfig config;

		Metadata metadata = metadataSchemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
		if (metadata.getInheritance() == null) {
			config = new MetadataDisplayConfig(collection, metadataCode, false, getDefaultMetadataInputType(
					metadataCode, metadataSchemasManager), false, "",
					getDefaultMetadataDisplayType(), getDefaultMetadataSortingType());
		} else {
			MetadataDisplayConfig inheritedConfig = getMetadata(metadata.getInheritance().getCode(), metadataSchemasManager);
			config = MetadataDisplayConfig.inheriting(metadataCode, inheritedConfig);
		}

		return config;
	}

	private MetadataInputType getDefaultMetadataInputType(String metadataCode,
														  MetadataSchemasManager metadataSchemasManager) {

		Metadata metadata = metadataSchemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
		List<MetadataInputType> types = MetadataInputType
				.getAvailableMetadataInputTypesFor(metadata.getType(), metadata.isMultivalue());
		if (types.isEmpty()) {
			types.add(MetadataInputType.FIELD);
		}

		return types.get(0);
	}

	private MetadataDisplayType getDefaultMetadataDisplayType() {
		return MetadataDisplayType.VERTICAL;
	}

	private MetadataSortingType getDefaultMetadataSortingType() {
		return MetadataSortingType.ENTRY_ORDER;
	}

}