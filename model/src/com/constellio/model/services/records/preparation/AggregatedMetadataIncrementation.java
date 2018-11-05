package com.constellio.model.services.records.preparation;

import com.constellio.model.entities.schemas.Metadata;
import lombok.Data;

@Data
public class AggregatedMetadataIncrementation {
	private String recordId;
	private Metadata metadata;
	private double amount;
}
