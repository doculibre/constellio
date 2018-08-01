package com.constellio.app.ui.framework.buttons;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class FormOrderButton extends Button {

	public static final String BUTTON_STYLE = "edit-button";

	public FormOrderButton() {
		this($("ListSchemaView.button.form"));
	}

	public FormOrderButton(String caption) {
		super(caption);
		addStyleName(ValoTheme.BUTTON_PRIMARY);
		addStyleName(BUTTON_STYLE);
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				FormOrderButton.this.buttonClick(event);
			}
		});
	}

	protected abstract void buttonClick(ClickEvent event);

}
