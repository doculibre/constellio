package com.constellio.app.ui.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field.ValueChangeEvent;

public class EventUtils implements Serializable {
	
	public static void setOptionsWithoutNotifyingListeners(AbstractSelect field, List<?> options) {
		Object value = field.getValue();
		Collection<?> listeners = field.getListeners(ValueChangeEvent.class);
		for (Object listener : listeners) {
			field.removeValueChangeListener((ValueChangeListener) listener);
		}
		if (field.size() > 0) {
			field.removeAllItems();
		}
		field.addItems(options);
		field.setValue(value);
		for (Object listener : listeners) {
			field.addValueChangeListener((ValueChangeListener) listener);
		}
	}

}
