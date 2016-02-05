package com.constellio.app.ui.framework.components.fields.list;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@SuppressWarnings("unchecked")
public class ListAddRemoveTextArea extends ListAddRemoveField<String, TextArea> {

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected TextArea newAddEditField() {
		TextArea textArea = new BaseTextArea();
		textArea.setImmediate(false);
		return textArea;
	}

	protected Component newCaptionComponent(String itemId, String caption) {
		caption = StringUtils.replace(caption, "\n", "<br/>");
		Label captionLabel = new Label(caption, ContentMode.HTML);
		return captionLabel;
	}

}
