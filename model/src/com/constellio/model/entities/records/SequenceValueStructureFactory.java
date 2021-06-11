package com.constellio.model.entities.records;

import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory;
import com.constellio.model.entities.schemas.StructureInstanciationParams;

public class SequenceValueStructureFactory extends AbstractMapBasedSeparatedStructureFactory {

	@Override
	protected MapBasedStructure newEmptyStructure(StructureInstanciationParams params) {
		return new SequenceValue();
	}

	@Override
	public String getMainValueFieldName() {
		return SequenceValue.VALUE;
	}

}
