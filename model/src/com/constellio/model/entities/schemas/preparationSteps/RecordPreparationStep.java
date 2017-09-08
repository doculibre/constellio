package com.constellio.model.entities.schemas.preparationSteps;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;

public interface RecordPreparationStep extends Serializable {

	List<Metadata> getMetadatas();

}