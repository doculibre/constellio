package com.constellio.app.modules.tasks.ui.components.fields;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;

import static com.constellio.app.ui.i18n.i18n.$;

public class DefaultFavoritesButton extends Button {
	boolean isStarred = false;

	public DefaultFavoritesButton() {
		super();
		updateIcon();
		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				isStarred = !isStarred;
				updateIcon();
				if (isStarred) {
					addToDefaultFavorites();
				} else {
					removeFromDefaultFavorites();
				}
			}
		});
	}

	public void updateIcon() {
		if (isStarred) {
			setIcon(FontAwesome.STAR);
			setCaption($("CartView.removeFromDefaultFavorites"));
		} else {
			setIcon(FontAwesome.STAR_O);
			setCaption($("CartView.addToDefaultFavorites"));
		}
	}

	public void setStarred(boolean starred) {
		isStarred = starred;
		updateIcon();
	}

	public void addToDefaultFavorites() {
	}

	public void removeFromDefaultFavorites() {
	}
}
