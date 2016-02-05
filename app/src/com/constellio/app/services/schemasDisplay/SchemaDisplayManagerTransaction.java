package com.constellio.app.services.schemasDisplay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;

public class SchemaDisplayManagerTransaction {

	String collection;

	SchemaTypesDisplayConfig modifiedCollectionTypes;

	List<SchemaTypeDisplayConfig> modifiedTypes = new ArrayList<>();

	List<SchemaDisplayConfig> modifiedSchemas = new ArrayList<>();

	List<MetadataDisplayConfig> modifiedMetadatas = new ArrayList<>();

	Set<String> addedCodes = new HashSet<>();

	public SchemaTypesDisplayConfig getModifiedCollectionTypes() {
		return modifiedCollectionTypes;
	}

	public List<SchemaTypeDisplayConfig> getModifiedTypes() {
		return modifiedTypes;
	}

	public List<SchemaDisplayConfig> getModifiedSchemas() {
		return modifiedSchemas;
	}

	public List<MetadataDisplayConfig> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public void add(SchemaTypeDisplayConfig typeDisplayConfig) {
		modifying(typeDisplayConfig.getSchemaType());
		modifiedTypes.add(typeDisplayConfig);
	}

	public void addReplacing(SchemaDisplayConfig schemaDisplayConfig) {
		addedCodes.add(schemaDisplayConfig.getSchemaCode());
		for (Iterator<SchemaDisplayConfig> iterator = modifiedSchemas.iterator(); iterator.hasNext(); ) {
			SchemaDisplayConfig modifiedSchema = iterator.next();
			if (modifiedSchema.getSchemaCode().equals(schemaDisplayConfig.getSchemaCode())) {
				iterator.remove();
			}
		}
		modifiedSchemas.add(schemaDisplayConfig);
	}

	public void add(SchemaDisplayConfig schemaDisplayConfig) {
		modifying(schemaDisplayConfig.getSchemaCode());
		modifiedSchemas.add(schemaDisplayConfig);
	}

	public void addReplacing(MetadataDisplayConfig metadataDisplayConfig) {
		addedCodes.add(metadataDisplayConfig.getMetadataCode());
		for (Iterator<MetadataDisplayConfig> iterator = modifiedMetadatas.iterator(); iterator.hasNext(); ) {
			MetadataDisplayConfig modifiedMetadata = iterator.next();
			if (modifiedMetadata.getMetadataCode().equals(metadataDisplayConfig.getMetadataCode())) {
				iterator.remove();
			}
		}
		modifiedMetadatas.add(metadataDisplayConfig);
	}

	public SchemaDisplayManagerTransaction add(MetadataDisplayConfig metadataDisplayConfig) {
		modifying(metadataDisplayConfig.getMetadataCode());
		modifiedMetadatas.add(metadataDisplayConfig);
		return this;
	}

	public void setModifiedCollectionTypes(SchemaTypesDisplayConfig modifiedCollectionTypes) {
		this.modifiedCollectionTypes = modifiedCollectionTypes;
	}

	public String getCollection() {

		if (modifiedCollectionTypes != null) {
			collection = modifiedCollectionTypes.getCollection();
		}

		for (SchemaTypeDisplayConfig config : modifiedTypes) {
			ensureSameCollection(config.getCollection());
		}

		for (SchemaDisplayConfig config : modifiedSchemas) {
			ensureSameCollection(config.getCollection());
		}

		for (MetadataDisplayConfig config : modifiedMetadatas) {
			ensureSameCollection(config.getCollection());
		}
		return collection;
	}

	private void ensureSameCollection(String collection) {
		if (this.collection == null) {
			this.collection = collection;
		} else if (!this.collection.equals(collection)) {
			throw new RuntimeException("Configs must be in same collection");
		}

	}

	private void modifying(String code) {
		//		if (addedCodes.contains(code)) {
		//			throw new RuntimeException(
		//					"Config '" + code + "' is already in transaction, adding it would override the previously added config");
		//		}
		addedCodes.add(code);
	}

	public SchemaDisplayConfig getModifiedSchema(String schemaCode) {
		for (SchemaDisplayConfig schemaDisplayConfig : modifiedSchemas) {
			if (schemaDisplayConfig.getSchemaCode().equals(schemaCode)) {
				return schemaDisplayConfig;
			}
		}
		return null;
	}

	public MetadataDisplayConfig getMetadataDisplayConfig(String code) {
		for (MetadataDisplayConfig metadataDisplayConfig : modifiedMetadatas) {
			if (metadataDisplayConfig.getMetadataCode().equals(code)) {
				return metadataDisplayConfig;
			}
		}
		return null;
	}
}
