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
package com.constellio.app.services.schemasDisplay;

import static com.constellio.app.services.schemasDisplay.SchemaDisplayUtils.getCustomSchemaDefaultDisplay;
import static com.constellio.app.services.schemasDisplay.SchemaDisplayUtils.getDefaultSchemaDefaultDisplay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class SchemasDisplayManagerCache {

	private String collection;
	private SchemaTypesDisplayConfig collectionTypes;
	private Map<String, SchemaTypeDisplayConfig> types = new HashMap<>();
	private Map<String, SchemaDisplayConfig> schemas = new HashMap<>();
	private Map<String, MetadataDisplayConfig> metadatas = new HashMap<>();

	public SchemasDisplayManagerCache(String collection) {
		this.collection = collection;
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
		// TODO verify for the default name
		return new SchemaTypeDisplayConfig(collection, typeCode, Arrays.asList("Default"));
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

	public SchemaDisplayConfig getSchemaDefaultDisplay(String schemaCode, MetadataSchemasManager metadataSchemasManager) {
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

	private MetadataDisplayConfig getDefaultMetadata(String metadataCode,
			MetadataSchemasManager metadataSchemasManager) {
		MetadataDisplayConfig config;

		Metadata metadata = metadataSchemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
		if (metadata.getInheritance() == null) {
			config = new MetadataDisplayConfig(collection, metadataCode, false, getDefaultMetadataInputType(
					metadataCode, metadataSchemasManager), false, "");
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
}