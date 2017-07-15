package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.tepi.listbuilder.ListBuilder;

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
	private ComboBox selectedReportField;
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

		newReportTitle = new TextField($("ReportConfigurationView.newReportTitle"));

		final VerticalLayout viewLayout = new VerticalLayout();
		selectedReportField = new ComboBox($("ReportConfigurationView.selectedReport"));
		Container container = new BeanItemContainer<>(ReportVO.class, presenter.getReports());
		selectedReportField.setContainerDataSource(container);
		selectedReportField.setInputPrompt($("ReportConfigurationView.newReport"));
		selectedReportField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
		selectedReportField.setItemCaptionPropertyId("title");
		selectedReportField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (selectedReportField.getValue() != null){
					newReportTitle.setVisible(false);
					deleteButton.setVisible(true);
				} else{
					deleteButton.setVisible(false);
					newReportTitle.setVisible(true);
				}
				//Replace tables after setting visible or not because visibility is checked while building
				Component newTable = buildTables(deleteButton.isVisible());
				viewLayout.replaceComponent(tables, newTable);
				tables = newTable;
			}
		});
		viewLayout.addComponent(selectedReportField);
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
			if(!metadataTitles.add(form.getLabel())) {
				duplicatedTitles.add(form.getLabel());
			}
		}

		for (MetadataVO form : metadataVOProvider.listMetadataVO()) {
			select.addItem(form);
			if(!duplicatedTitles.contains(form.getLabel())) {
				select.setItemCaption(form, form.getLabel() );
			}
			else {
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
				if(values.size() == 0){
					showErrorMessage($("ReportConfigurationView.selectedValuesEmpty"));
				} else if(StringUtils.isBlank(getSelectedReport())){
					showErrorMessage($("ReportConfigurationView.reportTitleRequired"));
				} else{
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
		if(newReportTitle != null && newReportTitle.isVisible()){
			return (String) newReportTitle.getValue();
		}
		if(selectedReportField != null && selectedReportField.getValue() != null){
			return ((ReportVO) selectedReportField.getValue()).getTitle();
		}else{
			return null;
		}
	}
}