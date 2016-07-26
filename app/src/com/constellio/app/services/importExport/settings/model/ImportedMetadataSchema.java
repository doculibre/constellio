package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportedMetadataSchema {

	private String code;
	private List<ImportedMetadata> metadatas = new ArrayList<>();
	private String label;

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

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
