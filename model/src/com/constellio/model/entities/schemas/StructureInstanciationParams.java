package com.constellio.model.entities.schemas;

import com.constellio.data.dao.dto.records.RecordId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StructureInstanciationParams {

	@Getter
	RecordId id;

	@Getter
	String schemaType;

	@Getter
	String collection;

}
