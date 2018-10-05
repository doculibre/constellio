package com.constellio.app.modules.tasks.ui.components.fields;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;

public class StarredButton extends Button {
	boolean isStarred = false;

	public StarredButton() {
		super();
		updateIcon();
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				isStarred = !isStarred;
				updateIcon();
				updateDefaultFavorites();
			}
		});
	}

	public void updateIcon() {
		if (isStarred) {
			setIcon(FontAwesome.STAR);
		} else {
			setIcon(FontAwesome.STAR_O);
		}
	}

	public void setStarred(boolean starred) {
		isStarred = starred;
		updateIcon();
	}

	public void updateDefaultFavorites() {
	}
}
