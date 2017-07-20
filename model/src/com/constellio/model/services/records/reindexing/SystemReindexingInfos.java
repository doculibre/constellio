package com.constellio.model.services.records.reindexing;

public class SystemReindexingInfos {

	private String collection;

	private String schemaTypeLabel;

	private long progression;

	private long total;

	public SystemReindexingInfos(String collection, String schemaTypeLabel, long progression, long total) {
		this.collection = collection;
		this.schemaTypeLabel = schemaTypeLabel;
		this.progression = progression;
		this.total = total;
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaTypeLabel() {
		return schemaTypeLabel;
	}

	public long getProgression() {
		return progression;
	}

	public long getTotal() {
		return total;
	}
}
