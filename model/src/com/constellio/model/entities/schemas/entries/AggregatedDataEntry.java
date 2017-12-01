package com.constellio.model.entities.schemas.entries;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

import java.util.List;

import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType;

public class AggregatedDataEntry implements DataEntry {

	private List<String> inputMetadatas;

	private String referenceMetadata;

	private AggregationType agregationType;

	private Class<? extends AggregatedCalculator<?>> aggregatedCalculator;

	public AggregatedDataEntry(List<String> inputMetadatas, String referenceMetadata, AggregationType agregationType,
			Class<? extends AggregatedCalculator<?>> aggregatedCalculator) {
		this.inputMetadatas = inputMetadatas;
		this.referenceMetadata = referenceMetadata;
		this.agregationType = agregationType;
		this.aggregatedCalculator = aggregatedCalculator;

		String referenceMetadataSchema = new SchemaUtils().getSchemaCode(referenceMetadata);
		if (!referenceMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("referenceMetadata", referenceMetadata, REFERENCE);
		}

		if (inputMetadatas != null) {
			for (String inputMetadata : inputMetadatas) {
				String inputMetadataSchema = inputMetadata == null ? null : new SchemaUtils().getSchemaCode(inputMetadata);

				if (inputMetadataSchema != null && !inputMetadataSchema.endsWith("_default")) {
					throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata, NUMBER);
				}

				if (inputMetadataSchema != null && !inputMetadataSchema.equals(referenceMetadataSchema)) {
					throw new DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(inputMetadata, referenceMetadata);
				}
			}
		}
	}

	public AggregatedDataEntry(List<String> inputMetadata, String referenceMetadata, AggregationType agregationType) {
		this(inputMetadata, referenceMetadata, agregationType, null);
	}

	public String getFirstInputMetadata() {
		return inputMetadatas.isEmpty() ? null : inputMetadatas.get(0);
	}

	public List<String> getInputMetadatas() {
		return inputMetadatas;
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
