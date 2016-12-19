package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImportedMetadataSchema {

	private String code;
	private List<ImportedMetadata> metadatas = new ArrayList<>();
	private String label;
	private List<String> formMetadatas = new ArrayList<>();
	private List<String> displayMetadatas = new ArrayList<>();
	private List<String> searchMetadatas = new ArrayList<>();
	private List<String> tableMetadatas = new ArrayList<>();

	public ImportedMetadataSchema setCode(String code) {
		this.code = code;
		return this;
	}

	public String getCode() {
		return code;
	}

	public ImportedMetadataSchema addMetadata(ImportedMetadata importedMetadata) {
		metadatas.add(importedMetadata);
		return this;
	}

	public ImportedMetadata newMetadata(String localCode) {
		ImportedMetadata importedMetadata = new ImportedMetadata().setCode(localCode);
		metadatas.add(importedMetadata);
		return importedMetadata;
	}

	public ImportedMetadataSchema setAllMetadatas(List<ImportedMetadata> metadata) {
		this.metadatas = metadata;
		return this;
	}

	public List<ImportedMetadata> getAllMetadata() {
		return metadatas;
	}

	public ImportedMetadata getMetadata(String localCode) {
		for (ImportedMetadata metadata : metadatas) {
			if (localCode.equals(metadata.getCode())) {
				return metadata;
			}
		}
		return null;
	}

	public ImportedMetadataSchema setLabel(String label) {
		this.label = label;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public List<String> getFormMetadatas() {
		return formMetadatas;
	}

	public ImportedMetadataSchema setFormMetadatas(List<String> formMetadatas) {
		this.formMetadatas = formMetadatas;
		return this;
	}

	public List<String> getDisplayMetadatas() {
		return displayMetadatas;
	}

	public ImportedMetadataSchema setDisplayMetadatas(List<String> displayMetadatas) {
		this.displayMetadatas = displayMetadatas;
		return this;
	}

	public List<String> getSearchMetadatas() {
		return searchMetadatas;
	}

	public ImportedMetadataSchema setSearchMetadatas(List<String> searchMetadatas) {
		this.searchMetadatas = searchMetadatas;
		return this;
	}

	public List<String> getTableMetadatas() {
		return tableMetadatas;
	}

	public ImportedMetadataSchema setTableMetadatas(List<String> tableMetadatas) {
		this.tableMetadatas = tableMetadatas;
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
