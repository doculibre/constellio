package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.tepi.listbuilder.ListBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportConfigurationViewImpl extends BaseViewImpl implements ReportConfigurationView {

	ReportDisplayConfigPresenter presenter;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form_cancel";
	public static final String DELETE_BUTTON = "base-form_delete";
	private Field newReportTitle;
	private Component tables;
	private Button deleteButton;


	public ReportConfigurationViewImpl() {
		this.presenter = new ReportDisplayConfigPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ReportDisplayConfigView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setParameters(params);

		newReportTitle = new BaseTextField($("ReportConfigurationView.newReportTitle"));
		if (presenter.isEditMode()) {
			newReportTitle.setValue(presenter.getReport().getTitle());
		}

		final VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.addComponent(newReportTitle);

		viewLayout.setSizeFull();
		tables = buildTables(false);
		viewLayout.addComponent(tables);
		return viewLayout;
	}

	private Component buildTables(boolean isDeleteButtonVisible) {
		MetadataVODataProvider metadataVOProvider = presenter.getDataProvider();
		Set<String> metadataTitles = new HashSet<>();
		Set<String> duplicatedTitles = new HashSet<>();

		final ListBuilder select = new ListBuilder();
		select.setColumns(30);
		select.setRightColumnCaption($("SearchDisplayConfigView.rightColumn"));
		select.setLeftColumnCaption($("SearchDisplayConfigView.leftColumn"));

		for (MetadataVO form : metadataVOProvider.listMetadataVO()) {
			if (!metadataTitles.add(form.getLabel())) {
				duplicatedTitles.add(form.getLabel());
			}
		}
		select.setValue(new ArrayList<MetadataVO>());
		List<String> selectedMetadataCodes = null;
		if (presenter.isEditMode()) {
			selectedMetadataCodes = presenter.getInheritedMetadataCodesFor(
					presenter.getReport().getReportedMetadataVOCodeList());
		}

		for (MetadataVO form : metadataVOProvider.listMetadataVO()) {
			select.addItem(form);
			if (presenter.isEditMode() && selectedMetadataCodes != null) {
				if (selectedMetadataCodes.contains(form.getCode())) {
					List<MetadataVO> currentSelectedItem = new ArrayList<>((Collection<? extends MetadataVO>) select.getValue());
					currentSelectedItem.add(form);
					select.setValue(currentSelectedItem);
				}
			}
			if (!duplicatedTitles.contains(form.getLabel())) {
				select.setItemCaption(form, form.getLabel());
			} else {
				select.setItemCaption(form, form.getLabel()
											+ "(" + presenter.getSchemaName(form.getCode()) + ": " + form.getLocalCode() + ")");
			}
		}

		select.setValue(presenter.getReportMetadatas());

		Button saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				List<MetadataVO> values = (List) select.getValue();
				if (values.size() == 0) {
					showErrorMessage($("ReportConfigurationView.selectedValuesEmpty"));
				} else if (StringUtils.isBlank(getSelectedReport())) {
					showErrorMessage($("ReportConfigurationView.reportTitleRequired"));
				} else {
					presenter.saveButtonClicked(values);
				}
			}
		});

		Button cancelButton = new Button($("cancel"));
		cancelButton.addStyleName(CANCEL_BUTTON);
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		});

		deleteButton = new Button($("delete"));

		deleteButton.addStyleName(DELETE_BUTTON);
		deleteButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.deleteButtonClicked();
			}
		});

		deleteButton.setVisible(isDeleteButtonVisible);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponent(saveButton);
		buttonsLayout.addComponent(cancelButton);
		buttonsLayout.addComponent(deleteButton);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		viewLayout.addComponent(select);
		viewLayout.addComponent(buttonsLayout);

		return viewLayout;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	public String getSelectedReport() {
		return (String) newReportTitle.getValue();
	}
}