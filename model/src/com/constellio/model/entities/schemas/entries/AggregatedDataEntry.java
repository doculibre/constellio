package com.constellio.model.entities.schemas.entries;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType;

public class AggregatedDataEntry implements DataEntry {

	private String inputMetadata;

	private String referenceMetadata;

	private AggregationType agregationType;

	private Class<? extends AggregatedCalculator<?>> aggregatedCalculator;

	public AggregatedDataEntry(String inputMetadata, String referenceMetadata, AggregationType agregationType) {
		this.inputMetadata = inputMetadata;
		this.referenceMetadata = referenceMetadata;
		this.agregationType = agregationType;
		this.aggregatedCalculator = null;

		String inputMetadataSchema = inputMetadata == null ? null : new SchemaUtils().getSchemaCode(inputMetadata);
		String referenceMetadataSchema = new SchemaUtils().getSchemaCode(referenceMetadata);

		if (inputMetadataSchema != null && !inputMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata, NUMBER);
		}

		if (!referenceMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("referenceMetadata", referenceMetadata, REFERENCE);
		}

		if (inputMetadataSchema != null && !inputMetadataSchema.equals(referenceMetadataSchema)) {
			throw new DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(inputMetadata, referenceMetadata);
		}
	}

	public AggregatedDataEntry(String inputMetadata, String referenceMetadata, AggregationType agregationType, Class<? extends AggregatedCalculator<?>> aggregatedCalculator) {
		this.inputMetadata = inputMetadata;
		this.referenceMetadata = referenceMetadata;
		this.agregationType = agregationType;
		this.aggregatedCalculator = aggregatedCalculator;

		String inputMetadataSchema = inputMetadata == null ? null : new SchemaUtils().getSchemaCode(inputMetadata);
		String referenceMetadataSchema = new SchemaUtils().getSchemaCode(referenceMetadata);

		if (inputMetadataSchema != null && !inputMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata, NUMBER);
		}

		if (!referenceMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("referenceMetadata", referenceMetadata, REFERENCE);
		}

		if (inputMetadataSchema != null && !inputMetadataSchema.equals(referenceMetadataSchema)) {
			throw new DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(inputMetadata, referenceMetadata);
		}
	}

	public String getInputMetadata() {
		return inputMetadata;
	}

	public String getReferenceMetadata() {
		return referenceMetadata;
	}

	public AggregationType getAgregationType() {
		return agregationType;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.AGGREGATED;
	}

	public Class<? extends AggregatedCalculator<?>> getAggregatedCalculator() {
		return aggregatedCalculator;
	}
}
