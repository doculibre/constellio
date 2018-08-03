package com.constellio.model.entities.schemas.preparationSteps;

import com.constellio.model.entities.schemas.Metadata;

import java.io.Serializable;
import java.util.List;

public interface RecordPreparationStep extends Serializable {

	List<Metadata> getMetadatas();

}