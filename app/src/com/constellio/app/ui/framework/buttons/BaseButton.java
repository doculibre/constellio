package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Extension;
import com.vaadin.ui.Button;

@SuppressWarnings("serial")
public abstract class BaseButton extends Button {
	public BaseButton(String caption) {
		setCaption(caption);

		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				BaseButton.this.buttonClick(event);
			}
		});
	}

	@Override
	public void addExtension(Extension extension) {
		super.addExtension(extension);
	}

	protected abstract void buttonClick(ClickEvent event);
}
