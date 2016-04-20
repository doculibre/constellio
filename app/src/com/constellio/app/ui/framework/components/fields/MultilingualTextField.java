package com.constellio.app.ui.framework.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

public class MultilingualTextField extends CustomField<Map<String, String>> {
	private final Map<String, String> value;
	private VerticalLayout layout;

	public MultilingualTextField() {
		value = new HashMap<>();
		for (String language : getCollectionLanguages()) {
			value.put(language, null);
		}
	}

	public void clear() {
		for (String language : getCollectionLanguages()) {
			value.put(language, null);
		}
		prepareEntryFields();
	}

	@Override
	public Map<String, String> getValue() {
		return value;
	}

	@Override
	public void setValue(Map<String, String> newFieldValue)
			throws ReadOnlyException, ConversionException {
		value.putAll(newFieldValue);
		prepareEntryFields();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Map<String, String>> getType() {
		return (Class) Map.class;
	}

	@Override
	protected Component initContent() {
		layout = new VerticalLayout();
		layout.setSpacing(true);
		prepareEntryFields();
		return layout;
	}

	private void prepareEntryFields() {
		layout.removeAllComponents();
		for (final String language : getCollectionLanguages()) {
			final BaseTextField field = new BaseTextField($(language));
			field.setValue(value.get(language));
			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					value.put(language, field.getValue());
				}
			});
			layout.addComponent(field);
		}
	}

	private List<String> getCollectionLanguages() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		return ConstellioFactories.getInstance().getModelLayerFactory().getCollectionsListManager()
				.getCollectionLanguages(collection);
	}
}
