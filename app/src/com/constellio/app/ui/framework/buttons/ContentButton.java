package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.ContentViewer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;

public class ContentButton extends WindowButton {
	private final String report, contentId, contentName;
	//	private final RMReportsPresenter presenter;

	public ContentButton(String report, String contentId, String contentName) {
		super($(report), $(report), WindowConfiguration.modalDialog("75%", "75%"));
		this.report = report;
		this.contentId = contentId;
		this.contentName = contentName;

		String iconPathKey = report + ".icon";
		String iconPath = $(iconPathKey);
		if (!iconPathKey.equals(iconPath)) {
			setIcon(new ThemeResource(iconPath));
		}
		addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
	}

	@Override
	protected Component buildWindowContent() {
		return new ContentViewer(contentId, contentName);
	}
}