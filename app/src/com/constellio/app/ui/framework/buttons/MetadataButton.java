package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class MetadataButton extends Button {

	public static final String BUTTON_STYLE = "edit-button";

	public MetadataButton() {
		this($("ListSchemaView.button.metadata"));
	}

	public MetadataButton(String caption) {
		super(caption);
		addStyleName(ValoTheme.BUTTON_PRIMARY);
		addStyleName(BUTTON_STYLE);
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				MetadataButton.this.buttonClick(event);
			}
		});
	}

	protected abstract void buttonClick(ClickEvent event);

}
