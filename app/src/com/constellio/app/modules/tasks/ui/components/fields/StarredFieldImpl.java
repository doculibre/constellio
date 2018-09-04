package com.constellio.app.modules.tasks.ui.components.fields;


import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

public class StarredFieldImpl extends Button {
	boolean isStarred = false;

	public StarredFieldImpl(final String taskId, List<String> value, String currentUser) {
		super();
		this.setStyleName(ValoTheme.BUTTON_BORDERLESS);
		if (value != null && value.contains(currentUser)) {
			isStarred = true;
		} else {
			isStarred = false;
		}
		updateIcon();

		addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				isStarred = !isStarred;
				updateIcon();
				updateTaskStarred(isStarred, taskId);
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

	public void updateTaskStarred(boolean isStarred, String taskId) {

	}
}
