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
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListReportSelector extends VerticalLayout {

	private final DecommissioningListPresenter presenter;
	private AbstractSelect includedFolderSelector;
	private AbstractSelect excludedFolderSelector;
	private AbstractSelect undefinedFolderSelector;
	private final Button button;

	public DecommissioningListReportSelector(DecommissioningListPresenter presenter) {
		setSpacing(true);
		this.presenter = presenter;
		button = buildActivationButton();
		try {
			Label label = new Label($("ReportTabButton.selectIncludedFolderTemplate"));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			includedFolderSelector = buildSelector();
			addComponents(label, buildSelectorWrapper(includedFolderSelector));

			label = new Label($("ReportTabButton.selectExcludedFolderTemplate"));
			label.addStyleName(ValoTheme.LABEL_BOLD);
			excludedFolderSelector = buildSelector();
			addComponents(label, buildSelectorWrapper(excludedFolderSelector));

			if (presenter.getDecommissionningListWithSelectedFolders()) {
				label = new Label($("ReportTabButton.selectUndefinedFolderTemplate"));
				label.addStyleName(ValoTheme.LABEL_BOLD);
				undefinedFolderSelector = buildSelector();
				addComponents(label, buildSelectorWrapper(undefinedFolderSelector));
			}

			addComponent(button);
		} catch (UnsupportedReportException e) {
			setVisible(false);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (includedFolderSelector != null) {
			includedFolderSelector.setEnabled(enabled);
		}
		if (excludedFolderSelector != null) {
			excludedFolderSelector.setEnabled(enabled);
		}
		if (undefinedFolderSelector != null) {
			undefinedFolderSelector.setEnabled(enabled);
		}
		button.setEnabled(enabled && isAllReportSelected());
	}

	private boolean isAllReportSelected() {
		return (includedFolderSelector == null || includedFolderSelector.getValue() != null) &&
			   (excludedFolderSelector == null || excludedFolderSelector.getValue() != null) &&
			   (undefinedFolderSelector == null || undefinedFolderSelector.getValue() != null);
	}

	public String getIncludedFolderReport() {
		return includedFolderSelector == null ? "" : (String) includedFolderSelector.getValue();
	}

	public String getExcludedFolderReport() {
		return excludedFolderSelector == null ? "" : (String) excludedFolderSelector.getValue();
	}

	public String getUndefinedFolderReport() {
		return undefinedFolderSelector == null ? "" : (String) undefinedFolderSelector.getValue();
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
					Object reportParameters = presenter.getReportParameters(getIncludedFolderReport(),
							getExcludedFolderReport(), getUndefinedFolderReport());

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