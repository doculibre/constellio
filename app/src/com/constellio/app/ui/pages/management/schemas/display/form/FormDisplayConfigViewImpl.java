package com.constellio.app.ui.pages.management.schemas.display.form;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.*;

import com.constellio.app.ui.pages.base.SessionContext;
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

public class FormDisplayConfigViewImpl extends BaseViewImpl implements FormDisplayConfigView {

	FormDisplayConfigPresenter presenter;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form_cancel";

	public FormDisplayConfigViewImpl() {
		this.presenter = new FormDisplayConfigPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {

		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
	}

	@Override
	protected String getTitle() {
		return $("FormDisplayConfigView.viewTitle", presenter.getLabel());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());
		return viewLayout;
	}


	private Component buildTables() {
		final List<FormMetadataVO> metadataVOs = presenter.getMetadatas();
		final List<FormMetadataVO> valueMetadataVOs = presenter.getValueMetadatas();

		final ListBuilder select = new ListBuilder();
		select.setColumns(30);
		select.setRightColumnCaption($("FormDisplayConfigView.rightColumn"));
		select.setLeftColumnCaption($("FormDisplayConfigView.leftColumn"));

		// Ordre alphabétique
		Collections.sort(metadataVOs, new SortFormMetadataVO(getSessionContext()));

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
