package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class BaseButton extends Button {
	
	public BaseButton(String caption) {
		this(caption, null);
	}
	
	public BaseButton(String caption, Resource icon) {
		this(caption, icon, false);
	}
	
	public BaseButton(String caption, Resource icon, boolean iconOnly) {
		setCaption(caption);
		setIcon(icon);
		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		}

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
