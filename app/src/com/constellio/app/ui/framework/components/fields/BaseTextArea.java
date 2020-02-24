package com.constellio.app.ui.framework.components.fields;

import com.vaadin.data.Property;
import com.vaadin.ui.TextArea;

public class BaseTextArea extends TextArea {

	private String inputMask;

	public BaseTextArea() {
		super();
		init();
	}

	public BaseTextArea(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseTextArea(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseTextArea(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseTextArea(String caption) {
		super(caption);
		init();
	}

	private void init() {
		setNullRepresentation("");
	}

	public String getInputMask() {
		return inputMask;
	}

	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
	}
}
