package com.constellio.model.services.records.extractions;

import java.util.Collection;

/**
 * This class extracts information from its feeds provided by {@link ExtractorSupplier<T>}
 * Note that the class will be serialized in the XML format (check {@link DefaultMetadataPopulator} for
 * how the concrete class will be serialized to the XML format).
 */
public interface Extractor<T>{
	Collection<? extends Object> extractFrom(T feed);
}
