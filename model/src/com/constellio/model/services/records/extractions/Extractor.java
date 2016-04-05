package com.constellio.model.services.records.extractions;

/**
 * This class extracts information from its feeds provided by {@link ExtractorSupplier<T>}
 * Note that the class should be abstract so that JAXB library can convert it
 * to the xml (JAXB does not support interface).
 */
public abstract class Extractor<T>{
	public abstract Object extractFrom(T feed);
}
