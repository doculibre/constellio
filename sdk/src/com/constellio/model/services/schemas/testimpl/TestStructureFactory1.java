package com.constellio.model.services.schemas.testimpl;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class TestStructureFactory1 implements CombinedStructureFactory {
	@Override
	public ModifiableStructure build(String string) {
		return new ZeModifiableStructure(string);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return ((ZeModifiableStructure) structure).getValue();
	}
}
