package com.constellio.app.ui.framework.components.fields;

import com.vaadin.data.Property;
import com.vaadin.ui.*;

import static java.lang.Boolean.TRUE;

public class BaseNullableField<T> extends CustomField<T> {
	protected Field<T> field;
	private CheckBox checkBox;

	public BaseNullableField(Field<T> field) {
		this.field = field;
		checkBox = new CheckBox();
		checkBox.setImmediate(true);
	}

	@Override
	protected Component initContent() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(true);

		if (field.getCaption() != null) {
			checkBox.setCaption("");
		}

		checkBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (TRUE.equals(checkBox.getValue())) {
					field.setValue(null);
					field.setEnabled(false);
				} else {
					field.setEnabled(true);
				}
			}
		});

		mainLayout.addComponents(field, checkBox);
		mainLayout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
		mainLayout.setComponentAlignment(checkBox, Alignment.MIDDLE_CENTER);

		return mainLayout;
	}

	protected void setFieldToNull() {
		if (!field.isRequired()) {
			field.setValue(null);
		}
	}

	@Override
	public Class<? extends T> getType() {
		return field.getType();
	}

	public T getValue() {
		if (Boolean.TRUE.equals(checkBox.getValue())) {
			return null;
		} else {
			return field.getValue();
		}
	}

	@Override
	public void setVisible(boolean enabled) {
		super.setVisible(enabled);
		field.setVisible(enabled);
		checkBox.setVisible(enabled);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		field.setEnabled(enabled);
		checkBox.setEnabled(enabled);
	}
}
