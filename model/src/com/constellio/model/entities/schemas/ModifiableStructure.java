package com.constellio.model.entities.schemas;

import java.io.Serializable;

public interface ModifiableStructure extends Serializable {

	boolean isDirty();
}
