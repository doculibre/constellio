package com.constellio.app.modules.rm.ui.pages.reports;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

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
		CssLayout layout = new CssLayout();
		layout.addStyleName("view-group");

		for (String report : presenter.getSupportedReports()) {
			if (presenter.isWithSchemaType(report)) {
				String schemaType = presenter.getSchemaTypeValue(report);
				WindowButton windowButton = buildLookupButton(schemaType, report);

				setReportButtonStyle(report, windowButton);
				layout.addComponent(windowButton);
			} else {
				ReportButton button = new ReportButton(report, presenter);
				layout.addComponent(button);
			}
		}
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

	private WindowButton buildLookupButton(final String schemaType, final String title) {
		return new WindowButton($(title),
				$(title)) {
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
						ReportButton reportButton = new ReportButton(title, presenter);
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
