package com.constellio.model.entities.schemas;

import java.util.Map;

public interface SeparatedStructureFactory extends StructureFactory {

	ModifiableStructure build(Map<String, Object> fields, StructureInstanciationParams params);

	Map<String, Object> toFields(ModifiableStructure structure);

	String getMainValueFieldName();

	default MetadataValueType getMainValueFieldType() {
		return MetadataValueType.STRING;
	}

	default boolean isMainValueMultivalued() {
		return false;
	}

}
