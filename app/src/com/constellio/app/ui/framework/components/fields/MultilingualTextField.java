package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class MultilingualTextField extends CustomField<Map<String, String>> {
	private final Map<String, String> value;
	private VerticalLayout layout;
	private final boolean areFieldsSetToRequired;

	public MultilingualTextField(boolean areFieldsSetToRequired) {
		value = new HashMap<>();
		this.areFieldsSetToRequired = areFieldsSetToRequired;
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
	protected Map<String, String> getInternalValue() {
		return value;
	}

	@Override
	protected void setInternalValue(Map<String, String> newFieldValue) {
		value.putAll(newFieldValue);
		if (layout != null) {
			prepareEntryFields();
		}
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
					setInternalValue(value);
				}
			});
			field.setRequired(areFieldsSetToRequired);
			field.setId(language);
			layout.addComponent(field);
		}
	}

	private List<String> getCollectionLanguages() {
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		return ConstellioFactories.getInstance().getModelLayerFactory().getCollectionsListManager()
				.getCollectionLanguages(collection);
	}

	public void validateFields() throws Validator.InvalidValueException {
		if(areFieldsSetToRequired) {
			Iterator<Component> componentIterator = layout.iterator();
			while(componentIterator.hasNext()) {
				BaseTextField field = (BaseTextField) componentIterator.next();
				field.setRequiredError($("MultilingualTextField.requiredError", $("Language."+field.getId()).toLowerCase()));
				field.validate();
			}
		}
	}
}
