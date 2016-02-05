package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class SearchDisplayButton extends Button {

	public static final String BUTTON_STYLE = "edit-button";

	public SearchDisplayButton() {
		this($("ListSchemaView.button.search"));
	}

	public SearchDisplayButton(String caption) {
		super(caption);
		addStyleName(ValoTheme.BUTTON_PRIMARY);
		addStyleName(BUTTON_STYLE);
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				SearchDisplayButton.this.buttonClick(event);
			}
		});
	}

	protected abstract void buttonClick(ClickEvent event);

}
