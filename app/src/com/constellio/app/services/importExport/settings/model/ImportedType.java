package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.constellio.model.entities.schemas.MetadataSchema;

public class ImportedType {

	private String code;
	private String label;
	private List<ImportedTab> tabs = new ArrayList<>();
	private ImportedMetadataSchema defaultSchema;
	private List<ImportedMetadataSchema> customSchemata = new ArrayList<>();

	public String getCode() {
		return code;
	}

	public ImportedType setCode(String code) {
		this.code = code;
		return this;
	}

	public List<ImportedTab> getTabs() {
		return tabs;
	}

	public ImportedType setTabs(List<ImportedTab> importedTabs) {
		this.tabs = importedTabs;
		return this;
	}

	public void addTab(ImportedTab importedTab) {
		tabs.add(importedTab);
	}

	public ImportedTab getTab(String code) {
		for (ImportedTab importedTab : tabs) {
			if (importedTab.getCode().equals(code)) {
				return importedTab;
			}
		}
		return null;
	}

	public ImportedMetadataSchema newSchema(String code) {
		ImportedMetadataSchema schema = new ImportedMetadataSchema().setCode(code);
		this.customSchemata.add(schema);
		return schema;
	}

	public ImportedType addSchema(ImportedMetadataSchema customSchema) {
		this.customSchemata.add(customSchema);
		return this;
	}

	public List<ImportedMetadataSchema> getCustomSchemata() {
		return customSchemata;
	}

	public ImportedType setCustomSchemata(List<ImportedMetadataSchema> customSchemata) {
		this.customSchemata = customSchemata;
		return this;
	}

	public ImportedMetadataSchema getDefaultSchema() {
		return defaultSchema;
	}

	public ImportedType setDefaultSchema(ImportedMetadataSchema defaultSchema) {
		this.defaultSchema = defaultSchema;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public ImportedType setLabel(String label) {
		this.label = label;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public ImportedMetadataSchema getSchemaNotNull(String code) {
		ImportedMetadataSchema schema = getSchema(code);
		if (schema == null) {
			throw new RuntimeException("No such schema with code '" + code + "'");
		}
		return schema;
	}

	public ImportedMetadataSchema getSchema(String code) {
		if ("default".equals(code)) {
			return defaultSchema;
		}
		for (ImportedMetadataSchema importedMetadataSchema : customSchemata) {
			if (importedMetadataSchema.getCode().equals(code)) {
				return importedMetadataSchema;
			}
		}
		return null;
	}

	public ImportedMetadataSchema newDefaultSchema() {
		ImportedMetadataSchema schema = new ImportedMetadataSchema().setCode("default");
		this.defaultSchema = schema;
		return defaultSchema;
	}
}
