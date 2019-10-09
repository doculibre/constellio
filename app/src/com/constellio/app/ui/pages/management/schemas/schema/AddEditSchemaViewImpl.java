package com.constellio.app.ui.pages.management.schemas.schema;

import com.constellio.app.ui.entities.FormMetadataSchemaVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.MultilingualTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditSchemaViewImpl extends BaseViewImpl implements AddEditSchemaView {

	private FormMetadataSchemaVO schemaVO;

	AddEditSchemaPresenter presenter;
	@PropertyId("localCode")
	private BaseTextField localCodeField;
	@PropertyId("labels")
	private MultilingualTextField labelsField;
	@PropertyId("advancedSearch")
	private CheckBox advancedSearch;
	@PropertyId("simpleSearch")
	private CheckBox simpleSearch;

	public AddEditSchemaViewImpl() {
		this.presenter = new AddEditSchemaPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AddEditSchemaView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
		presenter.setParameters(paramsMap);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildForm());
		return viewLayout;
	}

	@Override
	public void setSchemaVO(FormMetadataSchemaVO schemaVO) {
		this.schemaVO = schemaVO;
	}

	private Component buildForm() {
		localCodeField = new BaseTextField($("AddEditSchemaView.localCode"));
		localCodeField.setId("localCode");
		localCodeField.addStyleName("localCode");
		localCodeField.setEnabled(presenter.isCodeEditable());
		localCodeField.setRequired(true);

		labelsField = new MultilingualTextField(true);
		labelsField.setId("labels");
		labelsField.addStyleName("labels");
		labelsField.setRequired(false);

		advancedSearch = new CheckBox($("AddEditSchemaView.advancedSearch"));
		simpleSearch = new CheckBox($("AddEditSchemaView.simpleSearch"));

		return new BaseForm<FormMetadataSchemaVO>(schemaVO, this, localCodeField, labelsField, advancedSearch, simpleSearch) {
			@Override
			protected void saveButtonClick(FormMetadataSchemaVO schemaVO)
					throws ValidationException {
				try {
					labelsField.validateFields();
				} catch (Validator.InvalidValueException e) {
					showErrorMessage(e.getMessage());
					return;
				}
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(FormMetadataSchemaVO metadataVO) {
				presenter.cancelButtonClicked();
			}
		};
	}
}
