package com.constellio.model.services.records.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CacheLoadingProgression {

	String loadedSchemaType;

	String collection;

	long current;

	long total;


}
