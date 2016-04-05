package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Collection;

/**
 * This class supplies content (such as text) to {@link Extractor}
 * Note that the class should be abstract so that JAXB library can convert it
 * to the xml (JAXB does not support interface).
 * @param <T>
 */
public abstract class ExtractorSupplier<T>{
	public abstract void init(LoadingCache<String, ParsedContent> cachedParsedContentProvider, MetadataSchema schema);
	public abstract Collection<T> getFeeds(Record record);
}
