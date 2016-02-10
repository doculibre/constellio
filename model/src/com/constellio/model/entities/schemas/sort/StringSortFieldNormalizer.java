package com.constellio.model.entities.schemas.sort;

public interface StringSortFieldNormalizer {

	String normalize(String rawValue);

	String normalizeNull();

}
