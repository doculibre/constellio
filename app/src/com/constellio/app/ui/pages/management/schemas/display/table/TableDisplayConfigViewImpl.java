package com.constellio.app.ui.pages.management.schemas.display.table;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.pages.management.schemas.display.SortFormMetadataVO;
import org.vaadin.tepi.listbuilder.ListBuilder;

import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TableDisplayConfigViewImpl extends BaseViewImpl implements TableDisplayConfigView {

	TableDisplayConfigPresenter presenter;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form_cancel";

	public TableDisplayConfigViewImpl() {
		this.presenter = new TableDisplayConfigPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {

		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
	}

	@Override
	protected String getTitle() {
		return $("TableDisplayConfigView.viewTitle", presenter.getLabel());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());
		return viewLayout;
	}

	private Component buildTables() {
		List<FormMetadataVO> metadataVOs = presenter.getMetadatas();
		List<FormMetadataVO> valueMetadataVOs = presenter.getValueMetadatas();

		Collections.sort(metadataVOs, new SortFormMetadataVO(getSessionContext()));

		final ListBuilder select = new ListBuilder();
		select.setColumns(30);
		select.setRightColumnCaption($("TableDisplayConfigView.rightColumn"));
		select.setLeftColumnCaption($("TableDisplayConfigView.leftColumn"));

		for (FormMetadataVO form : metadataVOs) {
			select.addItem(form);
			select.setItemCaption(form, form.getLabel(getSessionContext().getCurrentLocale().getLanguage()));
		}

		select.setValue(valueMetadataVOs);

		Button saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				List<FormMetadataVO> values = (List) select.getValue();
				presenter.saveButtonClicked(values);
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

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponent(saveButton);
		buttonsLayout.addComponent(cancelButton);

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

}
