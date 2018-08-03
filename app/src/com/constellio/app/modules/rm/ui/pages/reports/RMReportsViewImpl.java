package com.constellio.app.modules.rm.ui.pages.reports;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMReportsViewImpl extends BaseViewImpl implements RMReportsView {

	public static final String OK_BUTTON = "seleniumOkButton";
	private final RMNewReportsPresenter presenter;

	public RMReportsViewImpl() {
		presenter = new RMNewReportsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("RMReportsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		CssLayout panel = new CssLayout();
		layout.addStyleName("view-group");

		for (ReportWithCaptionVO report : presenter.getSupportedReports()) {
			if (presenter.isWithSchemaType(report.getTitle())) {
				String schemaType = presenter.getSchemaTypeValue(report.getTitle());
				WindowButton windowButton = buildLookupButton(schemaType, report);

				setReportButtonStyle(report.getTitle(), windowButton);
				panel.addComponent(windowButton);
			} else {
				ReportButton button = new ReportButton(report, presenter);
				//				setReportButtonStyle(report, button);
				panel.addComponent(button);
			}
		}
		layout.addComponent(panel);
		return layout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	private WindowButton buildLookupButton(final String schemaType, final ReportWithCaptionVO report) {
		return new WindowButton($(report.getCaption()),
				$(report.getCaption())) {
			@Override
			protected Component buildWindowContent() {

				final Field<?> lookupSchemaType = new LookupRecordField(schemaType);
				lookupSchemaType.setCaption($("search"));
				lookupSchemaType.setId("schemaType");
				lookupSchemaType.addStyleName("schemaType");

				BaseButton okButton = new BaseButton($("Ok")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.setSchemaTypeValue((String) lookupSchemaType.getValue());
						ReportButton reportButton = new ReportButton(report, presenter);
						reportButton.click();
						getWindow().close();
					}
				};
				okButton.addStyleName(OK_BUTTON);
				okButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(okButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.addComponents(lookupSchemaType, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
	}

	private void setReportButtonStyle(String report, WindowButton windowButton) {
		String iconPathKey = report + ".icon";
		String iconPath = $(iconPathKey);
		if (!iconPathKey.equals(iconPath)) {
			windowButton.setIcon(new ThemeResource(iconPath));
		}
		windowButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		windowButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
	}
}
