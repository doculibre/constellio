package com.constellio.model.services.records.extractions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DefaultMetadataPopulator implements MetadataPopulator {

	private Extractor extractor;

	private ExtractorSupplier feedsExtractor;

	private LoadingCache<String, ParsedContent> cachedContentManager;
	private boolean multiValue;

	public DefaultMetadataPopulator() {
	}

	public <T> DefaultMetadataPopulator(Extractor<T> extractor, ExtractorSupplier<T> feedsExtractor) {
		this.feedsExtractor = feedsExtractor;
		this.extractor = extractor;
	}

	public Extractor getExtractor() {
		return extractor;
	}

	public void setExtractor(Extractor extractor) {
		this.extractor = extractor;
	}

	public ExtractorSupplier getFeedsExtractor() {
		return feedsExtractor;
	}

	public void setFeedsExtractor(ExtractorSupplier feedsExtractor) {
		this.feedsExtractor = feedsExtractor;
	}

	@Override
	public void init(final ContentManager contentManager, MetadataSchema schema, boolean multiValue) {
		this.multiValue = multiValue;
		CacheLoader<String, ParsedContent> loader = new CacheLoader<String, ParsedContent>() {
			@Override
			public ParsedContent load(String key) {
				return contentManager.getParsedContentParsingIfNotYetDone(key);
			}
		};
		cachedContentManager = CacheBuilder.newBuilder().build(loader);
		feedsExtractor.init(cachedContentManager, schema);
	}

	@Override
	public Object getPopulationValue(Record record) {
		List<Object> results = new ArrayList<>();

		for (Object feed : feedsExtractor.getFeeds(record)) {
			Collection<? extends Object> value = extractor.extractFrom(feed);
			if (value != null)
				results.addAll(value);
		}

		return convert(results);
	}

	private Object convert(List<Object> contentPopulatedValues) {
		if (contentPopulatedValues == null || contentPopulatedValues.isEmpty()) {
			return null;

		} else if (multiValue) {

			List<Object> convertedValues = new ArrayList<>();
			for (Object contentPopulatedValue : contentPopulatedValues) {
				convertedValues.addAll(asStringList(contentPopulatedValue));
			}
			return convertedValues;

		} else if (contentPopulatedValues.isEmpty()) {
			return null;

		} else {
			return contentPopulatedValues.get(0);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	private static List<String> asStringList(Object value) {
		if (value == null) {
			return new ArrayList<>();
		} else {
			List<String> values = new ArrayList<>();

			for (String aValue : value.toString().split(";")) {
				values.add(aValue.replace("'", "").trim());
			}

			return values;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
