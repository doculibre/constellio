package com.constellio.model.entities.records.structures;

import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureInstanciationParams;

public class NestedRecordAuthorizationsStructureFactory extends AbstractMapBasedSeparatedStructureFactory<NestedRecordAuthorizations> {
	@Override
	protected NestedRecordAuthorizations newEmptyStructure(StructureInstanciationParams params) {
		return new NestedRecordAuthorizations(params);
	}

	@Override
	public String getMainValueFieldName() {
		return "principals";
	}

	@Override
	public MetadataValueType getMainValueFieldType() {
		return MetadataValueType.INTEGER;
	}

	@Override
	public boolean isMainValueMultivalued() {
		return true;
	}
}
