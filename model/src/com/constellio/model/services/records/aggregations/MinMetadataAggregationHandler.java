package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Iterator;
import java.util.Set;

public class MinMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MinMetadataAggregationHandler() {
		super("min");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		return getMin(params.getMetadata().getType(), params.getValues());

	}

	public static Object getMin(MetadataValueType valueType, java.util.Collection<Object> values) {

		Object min = null;
		Iterator<Object> objectIterator = values.iterator();

		while (objectIterator.hasNext()) {
			Object value = objectIterator.next();

			if (valueType == MetadataValueType.NUMBER) {
				if (value instanceof Number
					&& (min == null || (Double) min > ((Number) value).doubleValue())) {
					min = ((Number) value).doubleValue();
				}
			} else if (valueType == MetadataValueType.INTEGER) {
				if (value instanceof Number
					&& (min == null || (Integer) min > ((Number) value).intValue())) {
					min = ((Number) value).intValue();
				}
			} else if (valueType == MetadataValueType.DATE) {
				if (value instanceof LocalDate
					&& (min == null || ((LocalDate) min).isAfter((LocalDate) value))) {
					min = value;
				} else if (value instanceof LocalDateTime
						   && (min == null || ((LocalDate) min).isAfter(((LocalDateTime) value).toLocalDate()))) {
					min = ((LocalDateTime) value).toLocalDate();
				}

			} else if (valueType == MetadataValueType.DATE_TIME) {
				if (value instanceof LocalDate
					&& (min == null || ((LocalDateTime) min)
						.isAfter(((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT)))) {
					min = ((LocalDate) value).toLocalDateTime(LocalTime.MIDNIGHT);

				} else if (value instanceof LocalDateTime
						   && (min == null || ((LocalDateTime) min).isAfter((LocalDateTime) value))) {
					min = value;

				}

			}

		}

		return min;
	}

	@Override
	protected Object calculateForNonNumber(MetadataValueType metadataValueType, Set<Object> values) {
		return getMin(metadataValueType, values);
	}
}
