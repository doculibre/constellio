package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;

public class ReportButton extends WindowButton {
	private final String report;
	//	private final RMReportsPresenter presenter;
	private final ReportPresenter presenter;

	public ReportButton(String report, ReportPresenter presenter) {
		super($(report), $(report), WindowConfiguration.modalDialog("75%", "75%"));
		this.report = report;
		this.presenter = presenter;
		
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
		return new ReportViewer(presenter.getReport(report));
	}
}
