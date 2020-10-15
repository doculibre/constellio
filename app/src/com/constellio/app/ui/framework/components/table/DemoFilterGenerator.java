package com.constellio.app.ui.framework.components.table;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import org.tepi.filtertable.FilterGenerator;

public class DemoFilterGenerator implements FilterGenerator {

	@Override
	public Filter generateFilter(Object propertyId, Object value) {
		if ("id".equals(propertyId)) {
			/* Create an 'equals' filter for the ID field */
			if (value != null && value instanceof String) {
				try {
					return new Compare.Equal(propertyId,
							Integer.parseInt((String) value));
				} catch (NumberFormatException ignored) {
					// If no integer was entered, just generate default filter
				}
			}
		}
		// For other properties, use the default filter
		return null;
	}

	@Override
	public Filter generateFilter(Object propertyId, Field<?> originatingField) {
		Object value = originatingField.getValue();
		return generateFilter(propertyId, value);
	}

	@Override
	public AbstractField<?> getCustomFilterComponent(Object propertyId) {
		return null;
	}

	@Override
	public void filterRemoved(Object propertyId) {
	}

	@Override
	public void filterAdded(Object propertyId, Class<? extends Filter> filterType, Object value) {
	}

	@Override
	public Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
		return null;
	}

}
