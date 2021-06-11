package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Iterator;
import java.util.Set;

public class MaxMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MaxMetadataAggregationHandler() {
		super("max");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		return getMax(params.getMetadata().getType(), params.getValues());

	}

	public static Object getMax(MetadataValueType valueType, java.util.Collection<Object> values) {

		Object max = null;
		Iterator<Object> objectIterator = values.iterator();

		while (objectIterator.hasNext()) {
			Object value = objectIterator.next();

			if (valueType == MetadataValueType.NUMBER) {
				if (value instanceof Number
					&& (max == null || (Double) max < ((Number) value).doubleValue())) {
					max = ((Number) value).doubleValue();
				}
			} else if (valueType == MetadataValueType.INTEGER) {
				if (value instanceof Number
					&& (max == null || (Integer) max < ((Number) value).intValue())) {
					max = ((Number) value).intValue();
				}
			} else if (valueType == MetadataValueType.DATE) {
				if (value instanceof LocalDate
					&& (max == null || ((LocalDate) max).isBefore((LocalDate) value))) {
					max = value;
				} else if (value instanceof LocalDateTime
						   && (max == null || ((LocalDate) max).isBefore(((LocalDateTime) value).toLocalDate()))) {
					max = ((LocalDateTime) value).toLocalDate();
				}

			} else if (valueType == MetadataValueType.DATE_TIME) {
				if (value instanceof LocalDate
					&& (max == null || ((LocalDateTime) max)
						.isBefore(((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT)))) {
					max = ((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT);

				} else if (value instanceof LocalDateTime
						   && (max == null || ((LocalDateTime) max).isBefore((LocalDateTime) value))) {
					max = value;

				}

			}

		}

		return max;
	}

	@Override
	protected Object calculateForNonNumber(MetadataValueType metadataValueType, Set<Object> values) {
		return getMax(metadataValueType, values);
	}
}
