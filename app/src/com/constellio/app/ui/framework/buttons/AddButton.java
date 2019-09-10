package com.constellio.app.ui.framework.buttons;

import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class AddButton extends BaseButton {

	public static final String BUTTON_STYLE = "add-button";

	public AddButton() {
		this(true);
	}

	public AddButton(boolean primary) {
		this($("add"), primary);
	}

	public AddButton(String caption) {
		this(caption, true);
	}

	public AddButton(String caption, boolean primary) {
		super(caption);
		if (primary) {
			addStyleName(ValoTheme.BUTTON_PRIMARY);
		}
		addStyleName(BUTTON_STYLE);
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AddButton.this.buttonClick(event);
			}
		});
	}

	protected abstract void buttonClick(ClickEvent event);

}
