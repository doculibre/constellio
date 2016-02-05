package com.constellio.app.ui.framework.buttons;

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
	
	protected abstract void buttonClick(ClickEvent event);

}
