package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.ArrayList;
import java.util.List;


class DefaultRecordPopulator implements RecordPopulator {
	private Extractor extractor;
	private FeedsExtractor feedsExtractor;
	private Metadata metadata;

	public <T> DefaultRecordPopulator(Extractor<T> extractor, FeedsExtractor<T> feedsExtractor, Metadata metadata){
		this.feedsExtractor = feedsExtractor;
		this.extractor = extractor;
	}

	@Override
	public Object getPopulationValue(Record record){
		List<Object> results = new ArrayList<>();

		for (Object feed: feedsExtractor.getFeeds(record)){
			results.add(extractor.extractFrom(feed));
		}

		return convert(results);
	}

	private Object convert(List<Object> contentPopulatedValues) {
		if (contentPopulatedValues == null || contentPopulatedValues.equals(new ArrayList<String>())) {
			return null;

		} else if (metadata.isMultivalue()) {

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

}
