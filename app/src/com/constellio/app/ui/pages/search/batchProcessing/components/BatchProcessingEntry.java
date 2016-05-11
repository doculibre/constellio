package com.constellio.app.ui.pages.search.batchProcessing.components;

import java.io.Serializable;

public class BatchProcessingEntry implements Serializable {
	private final String id;
	private final String metadataOrigin;
	private final String label;
	private BatchProcessingAction mappingType;
	private String index;
	private String fixedValue;
	private String defaultValue;
	private String mappingTable;

	public BatchProcessingEntry(String metadata, String label) {
		this.id = null;
		this.metadataOrigin = metadata;
		this.label = label;
		mappingType = BatchProcessingAction.NO_VALUE;
	}

	public BatchProcessingEntry(String id, String metadata, String label, BatchProcessingAction mappingType, String index,
								String fixedValue, String defaultValue, String mappingTable) {
		this.id = id;
		this.metadataOrigin = metadata;
		this.label = label;
		this.mappingType = mappingType;
		this.index = index;
		this.fixedValue = fixedValue;
		this.defaultValue = defaultValue;
		this.mappingTable = mappingTable;
	}

	public String getId() {
		return id;
	}

	public String getMetadata() {
		return metadataOrigin;
	}

	public String getLabel() {
		return label;
	}

	public BatchProcessingAction getBatchProcessingAction() {
		return mappingType;
	}

	public void setBatchProcessingAction(BatchProcessingAction mappingType) {
		this.mappingType = mappingType;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(String fixedValue) {
		this.fixedValue = fixedValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getMappingTable() {
		return mappingTable;
	}

	public void setMappingTable(String mappingTable) {
		this.mappingTable = mappingTable;
	}
}
