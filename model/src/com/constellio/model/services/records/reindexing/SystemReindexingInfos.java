package com.constellio.model.services.records.reindexing;

import java.util.function.Supplier;

public class SystemReindexingInfos {

	private String collection;

	private String schemaTypeLabel;

	private long progression;

	private long total;

	private Supplier<SystemReindexingConsumptionInfos> consumptionSupplier;

	public SystemReindexingInfos(String collection, String schemaTypeLabel, long progression, long total,
								 Supplier<SystemReindexingConsumptionInfos> consumptionSupplier) {
		this.collection = collection;
		this.schemaTypeLabel = schemaTypeLabel;
		this.progression = progression;
		this.total = total;
		this.consumptionSupplier = consumptionSupplier;
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

	public Supplier<SystemReindexingConsumptionInfos> getConsumptionSupplier() {
		return consumptionSupplier;
	}
}
