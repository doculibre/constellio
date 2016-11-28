package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseRichTextArea;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("unchecked")
public class ListAddRemoveRichTextArea extends ListAddRemoveField<String, BaseRichTextArea> {

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected BaseRichTextArea newAddEditField() {
		BaseRichTextArea richTextArea = new BaseRichTextArea();
		richTextArea.setImmediate(false);
		return richTextArea;
	}
	
	protected Component newCaptionComponent(String itemId, String caption) {
		Label captionLabel = new Label(caption);
		captionLabel.setContentMode(ContentMode.HTML);
		return captionLabel;
	}

	@Override
	protected void setMainLayoutWidth(VerticalLayout mainLayout) {
		mainLayout.setWidth("100%");
	}

}
