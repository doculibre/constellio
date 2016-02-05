package com.constellio.app.services.schemasDisplay;

import java.util.List;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SchemaTypesDisplayTransactionBuilder {

	MetadataSchemaTypes types;
	SchemasDisplayManager schemasDisplayManager;

	SchemaDisplayManagerTransaction transaction;

	public SchemaTypesDisplayTransactionBuilder(MetadataSchemaTypes types, SchemasDisplayManager schemasDisplayManager) {
		this.transaction = new SchemaDisplayManagerTransaction();
		this.types = types;
		this.schemasDisplayManager = schemasDisplayManager;
	}

	public SchemaTypeDisplayTransactionBuilder in(String typeCode) {
		return new SchemaTypeDisplayTransactionBuilder(types.getSchemaType(typeCode), schemasDisplayManager, this);
	}

	public SchemaDisplayManagerTransaction build() {
		return transaction;
	}

	public void updateAllSchemas(String typeCode, SchemaDisplayAlteration schemaDisplayAlteration) {
		for (MetadataSchema schema : types.getSchemaType(typeCode).getAllSchemas()) {
			SchemaDisplayConfig config = updateSchemaDisplayConfig(schema);

			SchemaDisplayConfig modifiedConfig = schemaDisplayAlteration.alter(config);
			if (modifiedConfig != config) {
				transaction.addReplacing(modifiedConfig);
			}
		}
	}

	public SchemaDisplayConfig updateSchemaDisplayConfig(MetadataSchema schema) {
		for (SchemaDisplayConfig config : transaction.modifiedSchemas) {
			if (config.getSchemaCode().equals(schema.getCode())) {
				return config;
			}
		}
		return schemasDisplayManager.getSchema(types.getCollection(), schema.getCode());
	}

	public SchemaTypesDisplayTransactionBuilder add(SchemaTypeDisplayConfig typeDisplayConfig) {
		transaction.add(typeDisplayConfig);
		return this;
	}

	public SchemaTypesDisplayTransactionBuilder add(SchemaDisplayConfig schemaDisplayConfig) {
		transaction.add(schemaDisplayConfig);
		return this;
	}

	public SchemaTypesDisplayTransactionBuilder add(MetadataDisplayConfig metadataDisplayConfig) {
		transaction.add(metadataDisplayConfig);
		return this;
	}

	public SchemaTypesDisplayTransactionBuilder addReplacing(SchemaDisplayConfig schemaDisplayConfig) {
		transaction.addReplacing(schemaDisplayConfig);
		return this;
	}

	public SchemaTypesDisplayTransactionBuilder addReplacing(MetadataDisplayConfig metadataDisplayConfig) {
		transaction.addReplacing(metadataDisplayConfig);
		return this;
	}

	public SchemaTypesDisplayTransactionBuilder setModifiedCollectionTypes(
			SchemaTypesDisplayConfig modifiedCollectionTypes) {
		transaction.setModifiedCollectionTypes(modifiedCollectionTypes);
		return this;
	}

	public SchemaTypesDisplayConfig getModifiedCollectionTypes() {
		return transaction.getModifiedCollectionTypes();
	}

	public MetadataDisplayConfig getMetadataDisplayConfig(String code) {
		return transaction.getMetadataDisplayConfig(code);
	}

	public List<MetadataDisplayConfig> getModifiedMetadatas() {
		return transaction.getModifiedMetadatas();
	}

	public List<SchemaTypeDisplayConfig> getModifiedTypes() {
		return transaction.getModifiedTypes();
	}

	public SchemaDisplayConfig getModifiedSchema(String schemaCode) {
		return transaction.getModifiedSchema(schemaCode);
	}

	public String getCollection() {
		return transaction.getCollection();
	}

	public List<SchemaDisplayConfig> getModifiedSchemas() {
		return transaction.getModifiedSchemas();
	}
}
