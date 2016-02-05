package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseRichTextArea;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;

@SuppressWarnings("unchecked")
public class ListAddRemoveRichTextArea extends ListAddRemoveField<String, RichTextArea> {

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected RichTextArea newAddEditField() {
		RichTextArea richTextArea = new BaseRichTextArea();
		richTextArea.setImmediate(false);
		return richTextArea;
	}
	
	protected Component newCaptionComponent(String itemId, String caption) {
		Label captionLabel = new Label(caption);
		captionLabel.setContentMode(ContentMode.HTML);
		return captionLabel;
	}

}
