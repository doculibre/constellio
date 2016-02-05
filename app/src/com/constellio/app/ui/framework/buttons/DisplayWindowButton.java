package com.constellio.app.ui.framework.buttons;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayWindowButton extends WindowButton{
	String text;

	public DisplayWindowButton(String windowCaption,
			String textToDisplay) {
		super("", windowCaption);
		setIcon(DisplayButton.ICON_RESOURCE);
		addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		this.text = textToDisplay;
	}

	@Override
	protected Component buildWindowContent() {
		FormLayout content = new FormLayout();
		Label label = new Label(text);
		label.setContentMode(ContentMode.PREFORMATTED);
		content.addComponent(label);
		content.setSizeUndefined();
		content.setMargin(true);
		return content;
	}

}
