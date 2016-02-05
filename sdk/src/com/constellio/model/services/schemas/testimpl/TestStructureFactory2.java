package com.constellio.model.services.schemas.testimpl;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

public class TestStructureFactory2 implements StructureFactory {
	@Override
	public ModifiableStructure build(String string) {
		return new ZeModifiableStructure(string);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return ((ZeModifiableStructure) structure).getValue();
	}
}