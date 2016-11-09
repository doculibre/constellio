package com.constellio.model.entities.schemas.entries;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType;

public class AgregatedDataEntry implements DataEntry {

	private String inputMetadata;

	private String referenceMetadata;

	private AgregationType agregationType;

	public AgregatedDataEntry(String inputMetadata, String referenceMetadata, AgregationType agregationType) {
		this.inputMetadata = inputMetadata;
		this.referenceMetadata = referenceMetadata;
		this.agregationType = agregationType;

		String inputMetadataSchema = new SchemaUtils().getSchemaCode(inputMetadata);
		String referenceMetadataSchema = new SchemaUtils().getSchemaCode(referenceMetadata);

		if (!inputMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata);
		}

		if (!referenceMetadataSchema.endsWith("_default")) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("referenceMetadata", referenceMetadata);
		}

		if (!inputMetadataSchema.equals(referenceMetadataSchema)) {
			throw new DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(inputMetadata, referenceMetadata);
		}
	}

	public String getInputMetadata() {
		return inputMetadata;
	}

	public String getReferenceMetadata() {
		return referenceMetadata;
	}

	public AgregationType getAgregationType() {
		return agregationType;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.AGREGATED;
	}
}
