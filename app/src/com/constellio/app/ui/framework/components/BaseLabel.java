package com.constellio.app.ui.framework.components;

import com.vaadin.data.Property;
import com.vaadin.server.Extension;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class BaseLabel extends Label {

	public BaseLabel() {
	}

	public BaseLabel(String content) {
		super(content);
	}

	public BaseLabel(Property<?> contentSource) {
		super(contentSource);
	}

	public BaseLabel(String content, ContentMode contentMode) {
		super(content, contentMode);
	}

	public BaseLabel(Property<?> contentSource, ContentMode contentMode) {
		super(contentSource, contentMode);
	}

	@Override
	public void addExtension(Extension extension) {
		super.addExtension(extension);
	}

}
