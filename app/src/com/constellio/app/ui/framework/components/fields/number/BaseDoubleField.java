package com.constellio.app.ui.framework.components.fields.number;

import com.constellio.app.ui.framework.components.converters.BaseStringToDoubleConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseDoubleField extends BaseTextField {

	public BaseDoubleField() {
		init();
	}

	public BaseDoubleField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseDoubleField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseDoubleField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseDoubleField(String caption) {
		super(caption);
		init();
	}

	@Override
	public Double getConvertedValue() {
		return (Double) super.getConvertedValue();
	}

	private void init() {
		setConverter(new BaseStringToDoubleConverter());
		setWidth("100px");
		addValidator(new Validator() {
			@Override
			public void validate(Object value) throws InvalidValueException {
				if (value == null) {
					throw new InvalidValueException($("containerCapacity"));
				}
			}
		});
	}
}
