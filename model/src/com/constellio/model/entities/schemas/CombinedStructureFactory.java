package com.constellio.model.entities.schemas;

public interface CombinedStructureFactory extends StructureFactory {

	ModifiableStructure build(String string);

	String toString(ModifiableStructure structure);

}
