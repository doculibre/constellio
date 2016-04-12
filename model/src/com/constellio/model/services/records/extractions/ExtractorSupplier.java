package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.google.common.cache.LoadingCache;

import java.util.Collection;

/**
 * This class supplies content (such as text) to {@link Extractor}
 * Note that the class will be serialized in the XML format (check {@link DefaultMetadataPopulator} for
 * how the concrete class will be serialized to the XML format).
 */
public interface ExtractorSupplier<T>{
	void init(LoadingCache<String, ParsedContent> cachedParsedContentProvider, MetadataSchema schema);
	Collection<T> getFeeds(Record record);
}
