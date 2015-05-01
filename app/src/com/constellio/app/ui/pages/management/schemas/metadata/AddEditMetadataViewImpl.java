/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.schemas.metadata;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

public class AddEditMetadataViewImpl extends BaseViewImpl implements AddEditMetadataView {

	AddEditMetadataPresenter presenter;

	@PropertyId("localcode")
	private BaseTextField localcodeField;
	@PropertyId("label")
	private BaseTextField titleField;
	@PropertyId("valueType")
	private ComboBox valueType;
	@PropertyId("multivalue")
	private CheckBox multivalueType;
	@PropertyId("input")
	private ComboBox inputType;
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
	@PropertyId("facet")
	private CheckBox facetField;
	@PropertyId("enabled")
	private CheckBox enabledField;
	@PropertyId("metadataGroup")
	private OptionGroup metadataGroup;

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

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());
		return viewLayout;
	}

	private Component buildTables() {
		FormMetadataVO formMetadataVO = presenter.getFormMetadataVO();

		final boolean editMode = formMetadataVO != null;

		final boolean inherited;
		if (!editMode) {
			formMetadataVO = new FormMetadataVO();
			inherited = false;
		} else {
			inherited = presenter.isInherited(formMetadataVO.getCode());
		}

		localcodeField = new BaseTextField($("AddEditMetadataView.localcode"));
		localcodeField.setId("localcode");
		localcodeField.addStyleName("localcode");
		localcodeField.setEnabled(!editMode);
		localcodeField.setRequired(true);

		titleField = new BaseTextField($("AddEditMetadataView.title"));
		titleField.setRequired(true);
		titleField.setId("title");
		titleField.addStyleName("title");

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
				MetadataValueTypeChanged((MetadataValueType) event.getProperty().getValue(), multivalueType.getValue(),
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

		metadataGroup = new OptionGroup($("AddEditMetadataView.metadataGroup"), presenter.getMetadataGroupList());
		metadataGroup.setRequired(true);
		metadataGroup.setId("metadataGroup");
		metadataGroup.addStyleName("metadataGroup");

		multivalueType = new CheckBox();
		multivalueType.setCaption($("AddEditMetadataView.multivalue"));
		multivalueType.setRequired(false);
		multivalueType.setId("multivalue");
		multivalueType.addStyleName("multivalue");
		multivalueType.setEnabled(!editMode);
		multivalueType.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				MetadataValueTypeChanged((MetadataValueType) valueType.getValue(),
						(Boolean) event.getProperty().getValue(), inherited, editMode);
			}
		});
		multivalueType.setReadOnly(editMode);

		requiredField = new CheckBox();
		requiredField.setCaption($("AddEditMetadataView.required"));
		requiredField.setRequired(false);
		requiredField.setId("required");
		requiredField.addStyleName("required");

		enabledField = new CheckBox();
		enabledField.setCaption($("AddEditMetadataView.enabled"));
		enabledField.setRequired(false);
		enabledField.setId("enabled");
		enabledField.addStyleName("enabled");

		searchableField = new CheckBox();
		searchableField.setCaption($("AddEditMetadataView.searchable"));
		searchableField.setRequired(false);
		searchableField.setId("searchable");
		searchableField.addStyleName("searchable");
		searchableField.setEnabled(!inherited && !editMode);

		sortableField = new CheckBox();
		sortableField.setCaption($("AddEditMetadataView.sortable"));
		sortableField.setRequired(false);
		sortableField.setId("sortable");
		sortableField.addStyleName("sortable");
		sortableField.setEnabled(!inherited);

		advancedSearchField = new CheckBox();
		advancedSearchField.setCaption($("AddEditMetadataView.advanced"));
		advancedSearchField.setRequired(false);
		advancedSearchField.setId("advancedSearch");
		advancedSearchField.addStyleName("advancedSearch");
		advancedSearchField.setEnabled(!inherited);

		facetField = new CheckBox();
		facetField.setCaption($("AddEditMetadataView.facet"));
		facetField.setRequired(false);
		facetField.setId("facet");
		facetField.addStyleName("facet");
		facetField.setEnabled(!inherited);

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
		autocomplete.setEnabled(!inherited && !editMode);

		return new BaseForm<FormMetadataVO>(formMetadataVO, this, localcodeField, titleField, valueType, multivalueType,
				inputType, metadataGroup, refType, requiredField, enabledField, searchableField, sortableField,
				advancedSearchField, facetField, highlight, autocomplete) {
			@Override
			protected void saveButtonClick(FormMetadataVO metadataVO)
					throws ValidationException {
				presenter.saveButtonClicked(metadataVO, editMode);
			}

			@Override
			protected void cancelButtonClick(FormMetadataVO metadataVO) {
				presenter.cancelButtonClicked();
			}
		};
	}

	private void MetadataValueTypeChanged(MetadataValueType value, Boolean multivalue, boolean inherited, boolean editMode) {
		if (value != null) {
			if (multivalue == null) {
				multivalue = false;
			}

			inputType.setEnabled(true);
			inputType.removeAllItems();
			List<MetadataInputType> types = MetadataInputType.getAvailableMetadataInputTypesFor(value, multivalue);
			if (types.isEmpty()) {
				inputType.setEnabled(false);
				inputType.setRequired(false);
			}

			for (MetadataInputType type : types) {
				inputType.addItem(type);
				inputType.setItemCaption(type, $(MetadataInputType.getCaptionFor(type)));
			}

			if (!inherited) {
				this.enableCorrectFields(value, inherited, editMode);
			}

			this.setValueFields(value);
		}
	}

	private void enableCorrectFields(MetadataValueType value, boolean inherited, boolean editMode) {

		refType.setEnabled(false);
		refType.setRequired(false);
		searchableField.setEnabled(false);
		sortableField.setEnabled(true);
		facetField.setEnabled(true);

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
			searchableField.setEnabled(!inherited && !editMode);
			break;
		case CONTENT:
			multivalueType.setEnabled(true);
			sortableField.setEnabled(false);
			searchableField.setEnabled(!inherited && !editMode);
			facetField.setEnabled(false);
			break;
		case DATE:
			multivalueType.setEnabled(true);
			facetField.setEnabled(false);
			break;
		case DATE_TIME:
			multivalueType.setEnabled(true);
			facetField.setEnabled(false);
			break;
		case INTEGER:
			multivalueType.setEnabled(true);
			searchableField.setEnabled(!inherited && !editMode);
			break;
		case REFERENCE:
			multivalueType.setEnabled(true);
			sortableField.setEnabled(false);
			refType.setEnabled(true);
			refType.setRequired(true);
			break;
		case STRING:
			multivalueType.setEnabled(true);
			searchableField.setEnabled(!inherited && !editMode);
			break;
		case NUMBER:
			multivalueType.setEnabled(true);
			searchableField.setEnabled(!inherited && !editMode);
			break;
		case STRUCTURE:
			multivalueType.setEnabled(true);
			facetField.setEnabled(false);
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
			facetField.setValue(false);
			sortableField.setValue(false);
		case REFERENCE:
			sortableField.setValue(false);
			break;
		case DATE:
			facetField.setValue(false);
			break;
		case DATE_TIME:
			facetField.setValue(false);
			break;
		case INTEGER:
			break;
		case STRING:
			break;
		case NUMBER:
			break;
		case STRUCTURE:
			facetField.setValue(false);
			break;
		}
	}
}
