package com.constellio.app.ui.pages.management.schemas.metadata;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.MultilingualTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class AddEditMetadataViewImpl extends BaseViewImpl implements AddEditMetadataView {
	final AddEditMetadataPresenter presenter;

	@PropertyId("localcode")
	private BaseTextField localcodeField;
	@PropertyId("labels")
	private MultilingualTextField labelsField;
	@PropertyId("valueType")
	private ComboBox valueType;
	@PropertyId("multivalue")
	private CheckBox multivalueType;
	@PropertyId("input")
	private ComboBox inputType;
	@PropertyId("displayType")
	private ComboBox displayType;
	@PropertyId("reference")
	private ComboBox refType;
	@PropertyId("required")
	private CheckBox requiredField;
	@PropertyId("searchable")
	private CheckBox searchableField;
	@PropertyId("sortable")
	private CheckBox sortableField;
	@PropertyId("highlight")
	private CheckBox highlight;
	@PropertyId("autocomplete")
	private CheckBox autocomplete;
	@PropertyId("advancedSearch")
	private CheckBox advancedSearchField;
	@PropertyId("enabled")
	private CheckBox enabledField;
	@PropertyId("metadataGroup")
	private OptionGroup metadataGroup;
	@PropertyId("defaultValue")
	private Field<?> defaultValueField;
	@PropertyId("inputMask")
	private BaseTextField inputMask;
	@PropertyId("duplicable")
	private CheckBox duplicableField;
	@PropertyId("ParentMetadataLabel")
	private TextField parentMetadataLabel;

	@PropertyId("uniqueValue")
	private CheckBox uniqueField;

	private List<CheckBox> customAttributesField = new ArrayList<>();

	private MetadataForm metadataForm;
	private FormMetadataVO formMetadataVO;

	VerticalLayout viewLayout;

	public AddEditMetadataViewImpl() {
		this.presenter = new AddEditMetadataPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AddEditMetadataView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
		presenter.setMetadataCode(params.get("metadataCode"));

		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());
		return viewLayout;
	}

	private Component buildTables() {
		formMetadataVO = presenter.getFormMetadataVO();

		final boolean editMode = formMetadataVO != null;

		final boolean inherited;
		if (!editMode) {
			formMetadataVO = new FormMetadataVO(getSessionContext());
			inherited = false;
		} else {
			inherited = presenter.isInherited(formMetadataVO.getCode());
		}

		return newForm(editMode, inherited);
	}

	private void metadataValueTypeChanged(MetadataValueType value, Boolean multivalue, boolean inherited, boolean editMode) {
		if (value != null) {
			if (multivalue == null) {
				multivalue = false;
			}

			if (value == MetadataValueType.STRING || value == MetadataValueType.TEXT) {
				searchableField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						if (!searchableField.getValue()) {
							advancedSearchField.setValue(false);
						}

						advancedSearchField.setEnabled(searchableField.getValue());
					}
				});

				advancedSearchField.setEnabled(searchableField.getValue());

				if (!searchableField.getValue()) {
					advancedSearchField.setValue(false);
				}
			}

			if(value == MetadataValueType.REFERENCE && multivalue) {
				sortableField.setValue(false);
				sortableField.setEnabled(false);
			} else if(value == MetadataValueType.REFERENCE) {
				sortableField.setEnabled(true);
			}

			inputType.setEnabled(false);
			inputType.removeAllItems();
			inputType.setEnabled(true);
			List<MetadataInputType> types = MetadataInputType.getAvailableMetadataInputTypesFor(value, multivalue);
			if (types.isEmpty()) {
				inputType.setEnabled(false);
				inputType.setRequired(false);
			}

			for (MetadataInputType type : types) {
				inputType.addItem(type);
				inputType.setItemCaption(type, $(MetadataInputType.getCaptionFor(type)));
			}
			inputType.setValue(null);

			displayType.setEnabled(false);
			displayType.removeAllItems();
			displayType.setEnabled(true);
			List<MetadataDisplayType> displayTypes = MetadataDisplayType
					.getAvailableMetadataDisplayTypesFor(value, formMetadataVO.getInput());
			for (MetadataDisplayType type : displayTypes) {
				displayType.addItem(type);
				displayType.setItemCaption(type, $(MetadataDisplayType.getCaptionFor(type)));
			}
			if (displayTypes.size() < 2) {
				displayType.setEnabled(false);
				displayType.setVisible(false);
				displayType.setValue(displayType.getItemIds().iterator().next());
			} else {
				displayType.setEnabled(true);
				displayType.setVisible(true);
			}

			if (!inherited) {
				this.enableCorrectFields(value, inherited, editMode);
			}

			inputMask.setEnabled(MetadataValueType.STRING.equals(value));
			this.setValueFields(value);
		}
	}

	private void enableCorrectFields(MetadataValueType value, boolean inherited, boolean editMode) {
		refType.setEnabled(false);
		refType.setRequired(false);
		//searchableField.setEnabled(false);
		sortableField.setEnabled(true);

		switch (value) {
		case BOOLEAN:
			if (multivalueType.getValue()) {
				multivalueType.setValue(false);
			}
			multivalueType.setEnabled(false);
			sortableField.setEnabled(false);
			break;
		case TEXT:
			multivalueType.setEnabled(true);
			sortableField.setEnabled(false);
			break;
		case CONTENT:
			multivalueType.setEnabled(true);
			sortableField.setEnabled(false);
			break;
		case DATE:
			multivalueType.setEnabled(true);
			break;
		case DATE_TIME:
			multivalueType.setEnabled(true);
			break;
		case INTEGER:
			multivalueType.setEnabled(true);
			break;
		case REFERENCE:
			multivalueType.setEnabled(true);
			sortableField.setEnabled(true);
			refType.setEnabled(true);
			refType.setRequired(!editMode);
			break;
		case STRING:
			multivalueType.setEnabled(true);
			break;
		case NUMBER:
			multivalueType.setEnabled(true);
			break;
		case STRUCTURE:
			multivalueType.setEnabled(true);
			break;
		}
	}

	private void setValueFields(MetadataValueType value) {

		refType.setValue(null);
		searchableField.setValue(false);

		switch (value) {
		case BOOLEAN:
			sortableField.setValue(false);
		case TEXT:
			sortableField.setValue(false);
		case CONTENT:
			sortableField.setValue(false);
		case REFERENCE:
			break;
		case DATE:
			break;
		case DATE_TIME:
			break;
		case INTEGER:
			break;
		case STRING:
			break;
		case NUMBER:
			break;
		case STRUCTURE:
			break;
		}
	}

	private MetadataForm newForm(final boolean editMode, final boolean inherited) {
		localcodeField = new BaseTextField($("AddEditMetadataView.localcode"));
		localcodeField.setId("localcode");
		localcodeField.addStyleName("localcode");
		localcodeField.setEnabled(!editMode);
		localcodeField.setRequired(true);

		//$("AddEditMetadataView.title")
		labelsField = new MultilingualTextField(true);
		//		labelsField.setRequired(true);
		labelsField.setId("labels");
		labelsField.addStyleName("labels");

		if(inherited) {
			parentMetadataLabel = new TextField();
			parentMetadataLabel.setCaption($("AddEditMetadataView.parentLabel"));
			parentMetadataLabel.addStyleName("labels");
			parentMetadataLabel.setValue(presenter.getParentFormMetadataVO().getLabel(Language.French.getCode()));
			parentMetadataLabel.setEnabled(false);
		}

		valueType = new ComboBox();
		valueType.setCaption($("AddEditMetadataView.type"));
		valueType.setRequired(true);
		valueType.setId("valueType");
		valueType.addStyleName("valueType");
		valueType.setEnabled(!editMode);
		valueType.setReadOnly(editMode);
		valueType.setNullSelectionAllowed(false);

		valueType.addItem(MetadataValueType.BOOLEAN);
		valueType.setItemCaption(MetadataValueType.BOOLEAN, $("AddEditMetadataView.type.boolean"));
		valueType.addItem(MetadataValueType.TEXT);
		valueType.setItemCaption(MetadataValueType.TEXT, $("AddEditMetadataView.type.text"));
		valueType.addItem(MetadataValueType.CONTENT);
		valueType.setItemCaption(MetadataValueType.CONTENT, $("AddEditMetadataView.type.content"));
		valueType.addItem(MetadataValueType.DATE);
		valueType.setItemCaption(MetadataValueType.DATE, $("AddEditMetadataView.type.date"));
		valueType.addItem(MetadataValueType.DATE_TIME);
		valueType.setItemCaption(MetadataValueType.DATE_TIME, $("AddEditMetadataView.type.datetime"));
		valueType.addItem(MetadataValueType.REFERENCE);
		valueType.setItemCaption(MetadataValueType.REFERENCE, $("AddEditMetadataView.type.reference"));
		valueType.addItem(MetadataValueType.STRING);
		valueType.setItemCaption(MetadataValueType.STRING, $("AddEditMetadataView.type.string"));
		valueType.addItem(MetadataValueType.NUMBER);
		valueType.setItemCaption(MetadataValueType.NUMBER, $("AddEditMetadataView.type.number"));

		valueType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				metadataValueTypeChanged((MetadataValueType) event.getProperty().getValue(), multivalueType.getValue(),
						inherited, editMode);
			}
		});

		refType = new ComboBox();
		refType.setCaption($("AddEditMetadataView.reftype"));
		refType.setRequired(false);
		refType.setId("reference");
		refType.addStyleName("reference");
		refType.setEnabled(false);
		for (String code : presenter.getMetadataTypesCode()) {
			refType.addItems(code);
			refType.setItemCaption(code, presenter.getMetadataTypesCaption(code));
		}
		refType.setNullSelectionAllowed(false);
		refType.setPageLength(refType.size());
		refType.setReadOnly(editMode);

		inputType = new ComboBox();
		inputType.setCaption($("AddEditMetadataView.entry"));
		inputType.setRequired(true);
		inputType.setId("entry");
		inputType.addStyleName("entry");
		inputType.setEnabled(false);
		inputType.setNullSelectionAllowed(false);

		displayType = new ComboBox();
		displayType.setCaption($("AddEditMetadataView.displayType"));
		displayType.setRequired(true);
		displayType.setId("displayType");
		displayType.addStyleName("displayType");
		displayType.setEnabled(false);
		displayType.setNullSelectionAllowed(false);

		metadataGroup = new OptionGroup($("AddEditMetadataView.metadataGroup"), presenter.getMetadataGroupList());
		metadataGroup.setRequired(true);
		metadataGroup.setId("metadataGroup");
		metadataGroup.addStyleName("metadataGroup");
		metadataGroup.setCaption(presenter.getLabel());
		for (String itemCode : presenter.getMetadataGroupList()) {
			metadataGroup.setItemCaption(itemCode, presenter.getGroupLabel(itemCode));
		}

		multivalueType = new CheckBox();
		multivalueType.setCaption($("AddEditMetadataView.multivalue"));
		multivalueType.setRequired(false);
		multivalueType.setId("multivalue");
		multivalueType.addStyleName("multivalue");
		multivalueType.setEnabled(!editMode);
		multivalueType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				metadataValueTypeChanged((MetadataValueType) valueType.getValue(),
						(Boolean) event.getProperty().getValue(), inherited, editMode);
			}
		});
		multivalueType.setReadOnly(editMode);

		requiredField = new CheckBox();
		requiredField.setCaption($("AddEditMetadataView.required"));
		requiredField.setRequired(false);
		requiredField.setId("required");
		requiredField.addStyleName("required");
		requiredField.setEnabled(presenter.isMetadataRequiredStatusModifiable() || presenter.isFolderMediumTypes());

		enabledField = new CheckBox();
		enabledField.setCaption($("AddEditMetadataView.enabled"));
		enabledField.setRequired(false);
		enabledField.setId("enabled");
		enabledField.addStyleName("enabled");
		enabledField.setEnabled(presenter.isMetadataEnableStatusModifiable());

		sortableField = new CheckBox();
		sortableField.setCaption($("AddEditMetadataView.sortable"));
		sortableField.setRequired(false);
		sortableField.setId("sortable");
		sortableField.addStyleName("sortable");
		sortableField.setEnabled(!inherited);

		advancedSearchField = new CheckBox() {
			@Override
			public void setEnabled(boolean enabled) {
				if(!formMetadataVO.getLocalcode().equals(Schemas.PATH.getLocalCode())) {
					super.setEnabled(enabled);
				}
				super.setEnabled(true);
			}
		};
		advancedSearchField.setCaption($("AddEditMetadataView.advanced"));
		advancedSearchField.setRequired(false);
		advancedSearchField.setId("advancedSearch");
		advancedSearchField.addStyleName("advancedSearch");

		searchableField = new CheckBox();
		searchableField.setCaption($("AddEditMetadataView.searchable"));
		searchableField.setRequired(false);
		searchableField.setId("searchable");
		searchableField.addStyleName("searchable");

		highlight = new CheckBox();
		highlight.setCaption($("AddEditMetadataView.highlight"));
		highlight.setRequired(false);
		highlight.setId("highlight");
		highlight.addStyleName("highlight");
		highlight.setEnabled(!inherited);

		autocomplete = new CheckBox();
		autocomplete.setCaption($("AddEditMetadataView.autocomplete"));
		autocomplete.setRequired(false);
		autocomplete.setId("autocomplete");
		autocomplete.addStyleName("autocomplete");
		autocomplete.setEnabled(!inherited);

		duplicableField = new CheckBox();
		duplicableField.setCaption($("AddEditMetadataView.duplicable"));
		duplicableField.setRequired(false);
		duplicableField.setId("duplicable");
		duplicableField.addStyleName("duplicable");
		duplicableField.setEnabled(true);

		uniqueField = new CheckBox();
		uniqueField.setCaption($("AddEditMetadataView.unique"));
		uniqueField.setRequired(false);
		uniqueField.setId("unique");


		customAttributesField = new ArrayList<>();
		for (String attribute : presenter.getAvailableExtraAttributes()) {
			CheckBox attributeField = new CheckBox();
			attributeField.setCaption($("CustomMetadataAttribute." + attribute));
			attributeField.setRequired(false);
			attributeField.setId(attribute);
			attributeField.addStyleName(attribute);
			attributeField.setEnabled(true);

			attributeField.setValue(formMetadataVO.getCustomAttributes().contains(attribute));

			customAttributesField.add(attributeField);
			viewLayout.addComponent(attributeField);
		}

		MetadataFieldFactory factory = new MetadataFieldFactory();

		MetadataVO defaultValueMetadataVO = presenter.getDefaultValueMetadataVO(formMetadataVO);

		Field<?> previousDefaultValueField = defaultValueField;
		if (defaultValueMetadataVO != null && presenter.isDefaultValuePossible(formMetadataVO)) {
			try {
				defaultValueField = factory.build(defaultValueMetadataVO);
			} catch (Exception e) {
				e.printStackTrace();
				defaultValueField = null;
			}
		}

		if (defaultValueField == null) {
			defaultValueField = new BaseTextField();
			defaultValueField.setEnabled(false);
		}
		defaultValueField.setCaption($("AddEditMetadataView.defaultValue"));
		defaultValueField.setId("defaultValue");
		if (previousDefaultValueField != null) {
			if (defaultValueField == null) {
				formMetadataVO.setDefaultValue(null);
			} else { //if (!previousDefaultValueField.getClass().equals(defaultValueField.getClass())) {
				formMetadataVO.setDefaultValue(null);
			}
		}
		defaultValueField.setRequired(false);

		inputMask = new BaseTextField($("AddEditMetadataView.inputMask"));
		inputMask.setEnabled(false);

		List<Field<?>> fields = new ArrayList<>(asList((Field<?>) localcodeField, labelsField, valueType, multivalueType,
				inputType, inputMask, metadataGroup, refType, requiredField, duplicableField, enabledField, searchableField, sortableField,
				advancedSearchField, highlight, autocomplete, uniqueField));

		for (CheckBox customAttributeField : customAttributesField) {
			fields.add(customAttributeField);
		}

		fields.add(defaultValueField);
		fields.add(displayType);

        if(parentMetadataLabel != null) {
            fields.add(2, parentMetadataLabel);
        }
		metadataForm = new MetadataForm(formMetadataVO, this, fields.toArray(new Field[0])) {//, displayedHorizontallyField) {

			@Override
			public void reload() {
				metadataForm = newForm(editMode, inherited);
				viewLayout.replaceComponent(this, metadataForm);
			}

			@Override
			public void commit() {
				labelsField.commit();
				for (Field<?> field : fieldGroup.getFields()) {
					try {
						field.commit();
					} catch (SourceException | InvalidValueException e) {
					}
				}
			}

			@Override
			protected void saveButtonClick(FormMetadataVO viewObject) {

				for (CheckBox customAttributeField : customAttributesField) {
					if (Boolean.TRUE.equals(customAttributeField.getValue())) {
						formMetadataVO.addCustomAttribute(customAttributeField.getId());
					} else {
						formMetadataVO.removeCustomAttribute(customAttributeField.getId());
					}
				}

				try {
					labelsField.validateFields();
				} catch (InvalidValueException e) {
					showErrorMessage(e.getMessage());
					return;
				}
				presenter.preSaveButtonClicked(formMetadataVO, editMode);
			}

			@Override
			protected void cancelButtonClick(FormMetadataVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};

        boolean isShowUnique = true;

        if(editMode){
			isShowUnique = presenter.isShowUniqueComboBox();
		}

		if(!formMetadataVO.getLocalcode().toLowerCase().equals("code")) {
			uniqueField.setEnabled(formMetadataVO.getValueType() == MetadataValueType.STRING && isShowUnique);
			uniqueField.setVisible(formMetadataVO.getValueType() == MetadataValueType.STRING);
		} else  {
			uniqueField.setEnabled(false);
			uniqueField.setVisible(true);
		}

		inputType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.inputTypeValueChanged(formMetadataVO);
			}
		});
		valueType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.valueTypeValueChanged();
			}
		});
		multivalueType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.multivalueValueChanged(formMetadataVO);
			}
		});
		refType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.refTypeValueChanged();
			}
		});

		if (presenter.isMetadataSystemReserved()) {
			disableFieldsForSystemReservedMetadatas();
		}

		return metadataForm;
	}

	@Override
	public void reloadForm() {
		metadataForm.commit();
		metadataForm.reload();
	}

	//TODO Move in presenter
	public void disableFieldsForSystemReservedMetadatas() {

		localcodeField.setEnabled(false);
		valueType.setEnabled(false);
		refType.setEnabled(false);
		inputType.setEnabled(false);
		multivalueType.setEnabled(false);
		enabledField.setEnabled(false);
		highlight.setEnabled(false);
		autocomplete.setEnabled(false);
		duplicableField.setEnabled(false);
		inputMask.setEnabled(false);
		defaultValueField.setEnabled(false);

		if (!formMetadataVO.getLocalcode().equals(Schemas.LEGACY_ID.getLocalCode())) {
			requiredField.setEnabled(false);
			searchableField.setEnabled(false);
			if(!formMetadataVO.getLocalcode().equals(Schemas.PATH.getLocalCode())) {
				advancedSearchField.setEnabled(false);
			}
		}
	}
}
