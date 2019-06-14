package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportSelector extends HorizontalLayout {

	private final NewReportPresenter presenter;
	private AbstractSelect selector;
	private final Button button;
	private boolean isOpenInNewWindow;

	public ReportSelector(NewReportPresenter presenter) {
		this(presenter, true);
	}

	public ReportSelector(NewReportPresenter presenter, boolean isOpenInNewWindow) {
		setSpacing(true);
		this.isOpenInNewWindow = isOpenInNewWindow;
		this.presenter = presenter;
		button = buildActivationButton();
		try {
			selector = buildSelector();
			addComponents(selector, button);
		} catch (UnsupportedReportException e) {
			setVisible(false);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		selector.setEnabled(enabled);
		button.setEnabled(enabled && selector.getValue() != null);
	}

	public String getSelectedReport() {
		return (String) selector.getValue();
	}

	private AbstractSelect buildSelector() {
		ComboBox comboBox = new BaseComboBox();
		List<ReportWithCaptionVO> supportedReports = presenter.getSupportedReports();
		if (supportedReports.isEmpty()) {
			setVisible(false);
		} else {
			for (ReportWithCaptionVO report : supportedReports) {
				comboBox.addItem(report.getTitle());
				comboBox.setItemCaption(report.getTitle(), report.getCaption());
			}
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);
			comboBox.setValue(supportedReports.get(0).getTitle());
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					button.setEnabled(true);
				}
			});
			comboBox.addValidator(new Validator() {
				@Override
				public void validate(Object value) throws InvalidValueException {
					if (value == null) {
						throw new InvalidValueException($("ReportTabButton.invalidReportType"));
					}
				}
			});
		}
		return comboBox;
	}

	private Button buildActivationButton() {
		Button button;

		if (isOpenInNewWindow) {
			button = new WindowButton($("ReportSelector.go"), null, WindowConfiguration.modalDialog("75%", "75%")) {
				@Override
				protected Component buildWindowContent() {
					NewReportWriterFactory factory = presenter.getReport(getSelectedReport());
					if (factory == null) {
						return new Label($("ReportViewer.noReportFactoryAvailable"));
					} else {
						Object reportParameters = presenter.getReportParameters(getSelectedReport());
						return new ReportViewer(factory.getReportBuilder(reportParameters),
								factory.getFilename(reportParameters));
					}
				}

				@Override
				protected String getWindowCaption() {
					return $(getSelectedReport());
				}
			};
		} else {
			button = new BaseButton($("ReportSelector.go")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					NewReportWriterFactory factory = presenter.getReport(getSelectedReport());
					if (factory == null) {
						addComponent(new Label($("ReportViewer.noReportFactoryAvailable")));
					} else {
						Object reportParameters = presenter.getReportParameters(getSelectedReport());

						StreamSource source = ReportViewer.buildSource(factory.getReportBuilder(reportParameters));

						StreamResource streamResource = new StreamResource(source, factory.getFilename(reportParameters));
						streamResource.setCacheTime(0);
						Page.getCurrent().open(streamResource, "_blank", isOpenInNewWindow);
					}
				}
			};
		}

		button.addStyleName(ValoTheme.BUTTON_PRIMARY);
		return button;
	}

}
