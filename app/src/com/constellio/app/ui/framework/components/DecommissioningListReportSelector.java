package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
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

public class DecommissioningListReportSelector extends VerticalLayout {

	private final DecommissioningListPresenter presenter;
	private AbstractSelect includedFolderSelector;
	private AbstractSelect excludedFolderSelector;
	private final Button button;

	public DecommissioningListReportSelector(DecommissioningListPresenter presenter) {
		setSpacing(true);
		this.presenter = presenter;
		button = buildActivationButton();
		try {
			Label label = new Label($("ReportTabButton.selectIncludedTemplate"));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			includedFolderSelector = buildSelector();
			addComponents(label, buildSelectorWrapper(includedFolderSelector));

			label = new Label($("ReportTabButton.selectExcludedTemplate"));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			excludedFolderSelector = buildSelector();
			addComponents(label, buildSelectorWrapper(excludedFolderSelector));

			addComponent(button);
		} catch (UnsupportedReportException e) {
			setVisible(false);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		includedFolderSelector.setEnabled(enabled);
		excludedFolderSelector.setEnabled(enabled);
		button.setEnabled(enabled && isAllReportSelected());
	}

	private boolean isAllReportSelected()
	{
		return includedFolderSelector.getValue() != null && excludedFolderSelector.getValue() != null;
	}

	public String getIncludedFolderReport() {
		return (String) includedFolderSelector.getValue();
	}

	public String getExcludedFolderReport() {
		return (String) excludedFolderSelector.getValue();
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
					button.setEnabled(isAllReportSelected());
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

	private Component buildSelectorWrapper(AbstractSelect selector) {
		I18NHorizontalLayout layout = new I18NHorizontalLayout(selector);
		layout.setComponentAlignment(selector, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);

		return layout;
	}

	private Button buildActivationButton() {
		Button button = new BaseButton($("ReportSelector.go")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				NewReportWriterFactory factory = presenter.getReport(getIncludedFolderReport());
				if (factory == null) {
					addComponent(new Label($("ReportViewer.noReportFactoryAvailable")));
				} else {
					Object reportParameters = presenter.getReportParameters(getIncludedFolderReport(), getExcludedFolderReport());

					StreamSource source = ReportViewer.buildSource(factory.getReportBuilder(reportParameters));

					StreamResource streamResource = new StreamResource(source, factory.getFilename(reportParameters));
					streamResource.setCacheTime(0);
					Page.getCurrent().open(streamResource, "_blank", false);
				}
			}
		};

		button.addStyleName(ValoTheme.BUTTON_PRIMARY);
		return button;
	}

}