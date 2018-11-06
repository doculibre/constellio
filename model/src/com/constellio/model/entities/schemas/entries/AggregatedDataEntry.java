package com.constellio.model.entities.schemas.entries;

import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class AggregatedDataEntry implements DataEntry {

	private Map<String, List<String>> inputMetadatasByReferenceMetadata;
	private AggregationType aggregationType;
	private Class<? extends AggregatedCalculator<?>> aggregatedCalculator;

	public AggregatedDataEntry(List<String> inputMetadatas, String referenceMetadata, AggregationType aggregationType,
							   Class<? extends AggregatedCalculator<?>> aggregatedCalculator) {

		Map<String, List<String>> inputMetadatasByReferenceMetadata = new HashMap<>();
		inputMetadatasByReferenceMetadata.put(referenceMetadata,
				inputMetadatas != null ? inputMetadatas : new ArrayList<String>());

		initialize(inputMetadatasByReferenceMetadata, aggregationType, aggregatedCalculator);
	}

	public AggregatedDataEntry(List<String> inputMetadata, String referenceMetadata, AggregationType aggregationType) {
		this(inputMetadata, referenceMetadata, aggregationType, null);
	}

	public AggregatedDataEntry(Map<String, List<String>> inputMetadatasByReferenceMetadata,
							   AggregationType aggregationType,
							   Class<? extends AggregatedCalculator<?>> aggregatedCalculator) {

		initialize(inputMetadatasByReferenceMetadata, aggregationType, aggregatedCalculator);
	}

	public AggregatedDataEntry(Map<String, List<String>> inputMetadatasByReferenceMetadata,
							   AggregationType agregationType) {
		this(inputMetadatasByReferenceMetadata, agregationType, null);
	}

	private void initialize(Map<String, List<String>> inputMetadatasByReferenceMetadata,
							AggregationType aggregationType,
							Class<? extends AggregatedCalculator<?>> aggregatedCalculator) {
		this.inputMetadatasByReferenceMetadata = inputMetadatasByReferenceMetadata;
		this.aggregationType = aggregationType;
		this.aggregatedCalculator = aggregatedCalculator;

		for (String referenceMetadata : inputMetadatasByReferenceMetadata.keySet()) {
			String referenceMetadataSchema = new SchemaUtils().getSchemaCode(referenceMetadata);
			if (!referenceMetadataSchema.endsWith("_default")) {
				throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("referenceMetadata", referenceMetadata, REFERENCE);
			}

			for (String inputMetadata : inputMetadatasByReferenceMetadata.get(referenceMetadata)) {
				if (inputMetadata != null) {
					String inputMetadataSchema = new SchemaUtils().getSchemaCode(inputMetadata);

					if (inputMetadataSchema != null && !inputMetadataSchema.endsWith("_default")) {
						throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata, NUMBER);
					}

					if (inputMetadataSchema != null && !inputMetadataSchema.equals(referenceMetadataSchema)) {
						throw new DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(inputMetadata, referenceMetadata);
					}
				}
			}
		}
	}

	public String getFirstInputMetadata() {
		return !inputMetadatasByReferenceMetadata.isEmpty() ?
			   inputMetadatasByReferenceMetadata.values().iterator().next().get(0) : null;
	}

	public List<String> getInputMetadatas() {
		Set<String> inputMetadatas = new HashSet<>();
		for (List<String> metadatas : inputMetadatasByReferenceMetadata.values()) {
			inputMetadatas.addAll(metadatas);
		}
		return new ArrayList<>(inputMetadatas);
	}

	public List<String> getReferenceMetadatas() {
		return new ArrayList<>(inputMetadatasByReferenceMetadata.keySet());
	}

	public Map<String, List<String>> getInputMetadatasByReferenceMetadata() {
		return inputMetadatasByReferenceMetadata;
	}

	public List<String> getInputMetadatas(String referenceMetadata) {
		return inputMetadatasByReferenceMetadata.get(referenceMetadata);
	}

	public AggregationType getAgregationType() {
		return aggregationType;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.AGGREGATED;
	}

	public Class<? extends AggregatedCalculator<?>> getAggregatedCalculator() {
		return aggregatedCalculator;
	}
}
