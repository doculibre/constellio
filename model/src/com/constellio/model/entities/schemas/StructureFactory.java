package com.constellio.model.entities.schemas;

import java.io.Serializable;

public interface StructureFactory extends Serializable {

	ModifiableStructure build(String string);

	String toString(ModifiableStructure structure);

}
