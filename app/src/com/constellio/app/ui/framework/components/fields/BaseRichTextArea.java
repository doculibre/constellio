package com.constellio.app.ui.framework.components.fields;

import com.vaadin.data.Property;
import com.vaadin.ui.RichTextArea;

public class BaseRichTextArea extends RichTextArea {

	public BaseRichTextArea() {
		init();
	}

	public BaseRichTextArea(String caption) {
		super(caption);
		init();
	}

	public BaseRichTextArea(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseRichTextArea(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseRichTextArea(String caption, String value) {
		super(caption, value);
		init();
	}
	
	private void init() {
		setNullRepresentation("");
		setWidth("100%");
	}

}
