package com.constellio.app.ui.pages.management.schemas.metadata;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.MultilingualRichTextField;
import com.constellio.app.ui.framework.components.fields.MultilingualTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class AddEditMetadataViewImpl extends BaseViewImpl implements AddEditMetadataView {
	final AddEditMetadataPresenter presenter;

	@PropertyId("localcode")
	private BaseTextField localcodeField;
	@PropertyId("labels")
	private MultilingualTextField labelsField;
	@PropertyId("dataEntryType")
	private OptionGroup dataEntryGroup;
	@PropertyId("dataEntryReference")
	private ComboBox dataEntryRef;
	@PropertyId("dataEntrySource")
	private ComboBox dataEntrySource;
	@PropertyId("valueType")
	private ComboBox valueType;
	@PropertyId("multivalue")
	private CheckBox multivalueType;
	@PropertyId("input")
	private ComboBox inputType;
	@PropertyId("displayType")
	private ComboBox displayType;
	@PropertyId("sortingType")
	private ComboBox sortingType;
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
	@PropertyId("availableInSummary")
	private CheckBox availableInSummary;
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
	@PropertyId("helpMessages")
	private MultilingualRichTextField helpMessagesField;

	@PropertyId("readAccessRoles")
	private ListOptionGroup listOptionGroupRole;

	@PropertyId("maxLength")
	private BaseTextField maxLength;
	@PropertyId("measurementUnit")
	private BaseTextField measurementUnit;

	@PropertyId("uniqueValue")
	private UniqueFieldLayout uniqueField;

	@PropertyId("multiLingual")
	private CheckBox multiLingualField;

	private List<CheckBox> customAttributesField = new ArrayList<>();

	private MetadataForm metadataForm;
	private FormMetadataVO formMetadataVO;
	private MetadataFieldFactory fieldFactory;

	VerticalLayout viewLayout;

	private Map<Field<?>, Boolean> fieldsEnability = new HashMap<>();
	private Map<Field<?>, Boolean> typesEnability = new HashMap<>();

	public AddEditMetadataViewImpl() {
		this.presenter = new AddEditMetadataPresenter(this);
	}

	public void setFieldFactory(MetadataFieldFactory fieldFactory) {
		this.fieldFactory = fieldFactory;
	}

	@Override
	protected String getTitle() {
		return $("AddEditMetadataView.viewTitle");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
		if (StringUtils.isNotBlank(params.get("metadataCode"))) {
			presenter.setMetadataCode(params.get("metadataCode"));
		}
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());

		if (!presenter.isRoleAccessSupportedOnThisMetadata()) {
			listOptionGroupRole.setVisible(false);
		}

		return viewLayout;
	}

	public boolean isInEditMode() {
		return presenter.metadata != null;
	}

	private void setFieldEnability(Field<?> field, boolean isEnable) {
		fieldsEnability.put(field, isEnable);
		updateFieldEnability(field);
	}

	private void setTypeEnability(Field<?> field, boolean isEnable) {
		typesEnability.put(field, isEnable);
		updateFieldEnability(field);
	}

	private void updateFieldEnability(Field<?> field) {
		boolean isFieldEnable = fieldsEnability.getOrDefault(field, true);
		boolean isTypeEnable = typesEnability.getOrDefault(field, true);
		field.setEnabled(isFieldEnable && isTypeEnable);
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

	private void metadataValueTypeChanged(MetadataValueType value, Boolean multivalue, boolean inherited,
										  boolean editMode) {
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

			enableDisableMaxLength();
			measurementUnit.setEnabled(value == MetadataValueType.INTEGER || value == MetadataValueType.NUMBER);

			if (value == MetadataValueType.CONTENT) {
				listOptionGroupRole.setVisible(false);
			}

			if (value == MetadataValueType.REFERENCE && multivalue) {
				sortableField.setValue(false);
				sortableField.setEnabled(false);
			} else if (value == MetadataValueType.REFERENCE) {
				sortableField.setEnabled(true);
			}

			inputType.setEnabled(false);
			inputType.removeAllItems();
			setFieldEnability(inputType, true);
			List<MetadataInputType> types = MetadataInputType.getAvailableMetadataInputTypesFor(value, multivalue);
			if (types.isEmpty()) {
				setFieldEnability(inputType, false);
				inputType.setRequired(false);
			}

			for (MetadataInputType type : types) {
				inputType.addItem(type);
			}
			inputType.setValue(null);

			displayType.setEnabled(false);
			displayType.removeAllItems();
			setFieldEnability(displayType, true);
			List<MetadataDisplayType> displayTypes = MetadataDisplayType
					.getAvailableMetadataDisplayTypesFor(value, formMetadataVO.getInput());
			for (MetadataDisplayType type : displayTypes) {
				displayType.addItem(type);
				displayType.setItemCaption(type, $(MetadataDisplayType.getCaptionFor(type)));
			}
			if (displayTypes.size() < 2) {
				setFieldEnability(displayType, false);
				displayType.setVisible(false);
				displayType.setValue(displayType.getItemIds().iterator().next());
			} else {
				setFieldEnability(displayType, true);
				displayType.setVisible(true);
			}

			sortingType.setEnabled(false);
			sortingType.removeAllItems();
			sortingType.setEnabled(true);
			List<MetadataSortingType> sortingTypes = MetadataSortingType
					.getAvailableMetadataSortingTypesFor(value, Boolean.TRUE.equals(multivalue));
			for (MetadataSortingType type : sortingTypes) {
				sortingType.addItem(type);
				sortingType.setItemCaption(type, $(MetadataSortingType.getCaptionFor(type)));
			}
			if (sortingTypes.size() < 2) {
				sortingType.setEnabled(false);
				sortingType.setVisible(false);
				sortingType.setValue(sortingType.getItemIds().iterator().next());
			} else {
				sortingType.setEnabled(true);
				sortingType.setVisible(true);
			}

			if (!inherited) {
				this.enableCorrectFields(value, inherited, editMode, multivalue);
			}

			setFieldEnability(inputMask, MetadataValueType.STRING.equals(value));
			this.setValueFields(value);
		}

		updateFields(formMetadataVO.getDataEntryType());
	}

	private void enableCorrectFields(MetadataValueType value, boolean inherited, boolean editMode, Boolean multivalue) {
		setFieldEnability(refType, false);
		refType.setRequired(false);
		//searchableField.setEnabled(false);
		sortableField.setEnabled(true);
		availableInSummary.setEnabled(presenter.isAvailableInSummaryFlagButtonEnabled(value));
		availableInSummary.setValue(presenter.isAvailableInSummaryFlagAlwaysTrue(value));

		enableDisableMaxLength();
		measurementUnit.setEnabled(value == MetadataValueType.INTEGER || value == MetadataValueType.NUMBER);

		maxLength.setVisible(maxLength.isEnabled());
		measurementUnit.setVisible(measurementUnit.isEnabled());

		switch (value) {
			case BOOLEAN:
				setFieldEnability(multivalueType, false);
				sortableField.setEnabled(false);
				searchableField.setValue(false);
				searchableField.setEnabled(false);
				autocomplete.setValue(false);
				autocomplete.setEnabled(false);
				break;
			case TEXT:
				setFieldEnability(multivalueType, true);
				sortableField.setEnabled(false);
				break;
			case CONTENT:
				setFieldEnability(multivalueType, true);
				sortableField.setEnabled(false);
				break;
			case DATE:
				setFieldEnability(multivalueType, true);
				break;
			case DATE_TIME:
				setFieldEnability(multivalueType, true);
				break;
			case INTEGER:
				setFieldEnability(multivalueType, true);
				searchableField.setValue(false);
				searchableField.setEnabled(false);
				autocomplete.setValue(false);
				autocomplete.setEnabled(false);
				break;
			case REFERENCE:
				if (multivalue) {
					sortableField.setValue(false);
					sortableField.setEnabled(false);
				}
				setFieldEnability(multivalueType, true);
				autocomplete.setValue(false);
				autocomplete.setEnabled(false);
				searchableField.setValue(false);
				searchableField.setEnabled(false);
				setFieldEnability(refType, true);
				refType.setRequired(!editMode);
				break;
			case STRING:
				setFieldEnability(multivalueType, true);
				break;
			case NUMBER:
				setFieldEnability(multivalueType, true);
				searchableField.setValue(false);
				searchableField.setEnabled(false);
				autocomplete.setValue(false);
				autocomplete.setEnabled(false);
				break;
			case STRUCTURE:
				setFieldEnability(multivalueType, true);
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
			case DATE:
			case DATE_TIME:
			case INTEGER:
			case STRING:
			case NUMBER:
			case STRUCTURE:
				break;
		}
	}

	private void dataEntryTypeChanged(DataEntryType type) {
		if (type == DataEntryType.COPIED) {
			updateFields(type);
			dataEntryRef.select(null);
			dataEntrySource.select(null);
			dataEntrySource.setEnabled(false);

			valueType.addItem(MetadataValueType.ENUM);
			valueType.setItemCaption(MetadataValueType.ENUM, $("ENUM"));
		} else {
			updateFields(type);

			valueType.removeItem(MetadataValueType.ENUM);
		}
	}

	private void dataEntryRefChanged(String refMetadataCode) {
		if (StringUtils.isNotBlank(refMetadataCode)) {
			dataEntrySource.setEnabled(true);

			dataEntrySource.removeAllItems();
			for (String code : presenter.getSourceMetadataCodes(refMetadataCode)) {
				dataEntrySource.addItems(code);
				dataEntrySource.setItemCaption(code, presenter.getSourceMetadataCaption(refMetadataCode, code));
			}
			dataEntrySource.setPageLength(dataEntrySource.size());
		}
	}

	private void dataEntrySourceChanged(String refMetadataCode, String sourceMetadataCode) {
		if (StringUtils.isNotBlank(sourceMetadataCode)) {
			Metadata source = presenter.getSourceMetadata(refMetadataCode, sourceMetadataCode);
			if (source != null) {
				if (!inputType.isReadOnly()) {
					formMetadataVO.setInput(presenter.getInputType(source));
					inputType.setValue(formMetadataVO.getInput());
				}
				if (!valueType.isReadOnly()) {
					valueType.select(source.getType());
				}
				if (!multivalueType.isReadOnly()) {
					multivalueType.setValue(source.isMultivalue());
				}
				updateFields(formMetadataVO.getDataEntryType());
			}
		}
	}

	private void updateFields(DataEntryType entryType) {
		final boolean isCopied = entryType == DataEntryType.COPIED;

		dataEntryRef.setVisible(isCopied);
		dataEntryRef.setRequired(isCopied);
		dataEntrySource.setVisible(isCopied);
		dataEntrySource.setRequired(isCopied);

		setTypeEnability(valueType, !isCopied);
		setTypeEnability(multivalueType, !isCopied);
		setTypeEnability(inputType, !isCopied);
		setTypeEnability(inputMask, !isCopied);
		setTypeEnability(refType, !isCopied);
		setTypeEnability(requiredField, !isCopied);
		setTypeEnability(defaultValueField, !isCopied);
		setTypeEnability(displayType, !isCopied);
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

		helpMessagesField = new MultilingualRichTextField();
		helpMessagesField.setId("helpMessages");
		helpMessagesField.addStyleName("helpMessages");

		if (inherited) {
			parentMetadataLabel = new BaseTextField();
			parentMetadataLabel.setCaption($("AddEditMetadataView.parentLabel"));
			parentMetadataLabel.addStyleName("labels");
			parentMetadataLabel.setValue(presenter.getParentFormMetadataVO().getLabel(Language.French.getCode()));
			parentMetadataLabel.setEnabled(false);
		}

		List<DataEntryType> dataEntryOption = new ArrayList<>();
		dataEntryOption.add(DataEntryType.MANUAL);
		dataEntryOption.add(DataEntryType.COPIED);
		dataEntryGroup = new OptionGroup($("AddEditMetadataView.dataEntryType"), dataEntryOption);
		dataEntryGroup.setRequired(true);
		dataEntryGroup.setId("dataEntryType");
		dataEntryGroup.addStyleName("horizontal");
		dataEntryGroup.setItemCaption(DataEntryType.MANUAL, $("AddEditMetadataView.dataEntryType.manual"));
		dataEntryGroup.setItemCaption(DataEntryType.COPIED, $("AddEditMetadataView.dataEntryType.copied"));
		dataEntryGroup.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataEntryTypeChanged((DataEntryType) event.getProperty().getValue());
			}
		});
		dataEntryGroup.setReadOnly(editMode);
		dataEntryGroup.setVisible(!editMode);

		dataEntryRef = new ComboBox();
		dataEntryRef.setCaption($("AddEditMetadataView.dataEntryRef"));
		dataEntryRef.setRequired(false);
		dataEntryRef.setId("dataEntryRef");
		dataEntryRef.setVisible(false);
		for (String code : presenter.getReferenceMetadataCodes()) {
			dataEntryRef.addItems(code);
			dataEntryRef.setItemCaption(code, presenter.getReferenceMetadataCaption(code));
		}
		dataEntryRef.setNullSelectionAllowed(false);
		dataEntryRef.setPageLength(dataEntryRef.size());
		dataEntryRef.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataEntryRefChanged((String) event.getProperty().getValue());
			}
		});
		dataEntryRef.setReadOnly(editMode);

		dataEntrySource = new ComboBox();
		dataEntrySource.setCaption($("AddEditMetadataView.dataEntrySource"));
		dataEntrySource.setRequired(false);
		dataEntrySource.setId("dataEntrySource");
		dataEntrySource.setVisible(false);
		dataEntrySource.setNullSelectionAllowed(false);
		dataEntrySource.setPageLength(dataEntrySource.size());
		dataEntrySource.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dataEntrySourceChanged((String) dataEntryRef.getValue(), (String) event.getProperty().getValue());
			}
		});
		dataEntrySource.setReadOnly(editMode);

		valueType = new ComboBox();
		valueType.setCaption($("AddEditMetadataView.type"));
		valueType.setRequired(true);
		valueType.setId("valueType");
		valueType.addStyleName("valueType");
		setFieldEnability(valueType, !editMode);
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
		setFieldEnability(refType, false);
		for (String code : presenter.getMetadataTypesCode()) {
			refType.addItems(code);
			refType.setItemCaption(code, presenter.getMetadataTypesCaption(code));
		}
		refType.setNullSelectionAllowed(false);
		refType.setPageLength(refType.size());
		refType.setReadOnly(editMode);

		inputType = new ComboBox() {
			@Override
			public String getItemCaption(Object itemId) {
				return $(MetadataInputType.getCaptionFor((MetadataInputType) itemId));
			}
		};
		inputType.setCaption($("AddEditMetadataView.entry"));
		inputType.setRequired(true);
		inputType.setId("entry");
		inputType.addStyleName("entry");
		setFieldEnability(inputType, false);
		inputType.setNullSelectionAllowed(false);

		displayType = new ComboBox();
		displayType.setCaption($("AddEditMetadataView.displayType"));
		displayType.setRequired(true);
		displayType.setId("displayType");
		displayType.addStyleName("displayType");
		setFieldEnability(displayType, false);
		displayType.setNullSelectionAllowed(false);

		sortingType = new ComboBox();
		sortingType.setCaption($("AddEditMetadataView.sortingType"));
		sortingType.setRequired(true);
		sortingType.setId("sortingType");
		sortingType.addStyleName("sortingType");
		sortingType.setEnabled(false);
		sortingType.setNullSelectionAllowed(false);

		metadataGroup = new OptionGroup($("AddEditMetadataView.metadataGroup"), presenter.getMetadataGroupList());
		metadataGroup.setRequired(true);
		metadataGroup.setId("metadataGroup");
		metadataGroup.addStyleName("metadataGroup");
		for (String itemCode : presenter.getMetadataGroupList()) {
			metadataGroup.setItemCaption(itemCode, presenter.getGroupLabel(itemCode));
		}

		multivalueType = new CheckBox();
		multivalueType.setCaption($("AddEditMetadataView.multivalue"));
		multivalueType.setRequired(false);
		multivalueType.setId("multivalue");
		multivalueType.addStyleName("multivalue");
		setFieldEnability(multivalueType, !editMode);
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
		setFieldEnability(requiredField, presenter.isMetadataRequiredStatusModifiable() || presenter.isFolderMediumTypes());

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
				if (!formMetadataVO.getLocalcode().equals(Schemas.PATH.getLocalCode())) {
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

		availableInSummary = new CheckBox();
		availableInSummary.setCaption($("AddEditMetadataView.availableInSummary"));
		availableInSummary.setRequired(false);
		availableInSummary.setId("availableInSummary");
		availableInSummary.addStyleName("availableInSummary");
		availableInSummary.setVisible(presenter.isAvailableInSummaryFlagButtonVisible());
		availableInSummary.setEnabled(!inherited && presenter.isAvailableInSummaryFlagButtonEnabled(
				formMetadataVO.getValueType()));
		if (presenter.isAvailableInSummaryFlagAlwaysTrue(formMetadataVO.getValueType())) {
			availableInSummary.setValue(true);
		}

		duplicableField = new CheckBox();
		duplicableField.setCaption($("AddEditMetadataView.duplicable"));
		duplicableField.setRequired(false);
		duplicableField.setId("duplicable");
		duplicableField.addStyleName("duplicable");
		duplicableField.setEnabled(true);

		uniqueField = new UniqueFieldLayout(() -> presenter.printDuplicateReport(this::showDuplicateReportDownloadLink));

		multiLingualField = new CheckBox();
		multiLingualField.setCaption($("AddEditMetadataView.multiLingual"));
		multiLingualField.setRequired(false);
		multiLingualField.setId("multiLingual");
		multiLingualField.setVisible(formMetadataVO.getValueType() == MetadataValueType.STRING
									 || formMetadataVO.getValueType() == MetadataValueType.TEXT);

		// TODO désactivé
		multiLingualField.setVisible(false);


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

		if (fieldFactory == null) {
			fieldFactory = new MetadataFieldFactory();
		}

		MetadataVO defaultValueMetadataVO = presenter.getDefaultValueMetadataVO(formMetadataVO, editMode);

		Field<?> previousDefaultValueField = defaultValueField;
		if (defaultValueMetadataVO != null && presenter.isDefaultValuePossible(formMetadataVO)) {
			try {
				defaultValueField = fieldFactory.build(defaultValueMetadataVO, null);
			} catch (Exception e) {
				e.printStackTrace();
				defaultValueField = null;
			}
		}

		if (defaultValueField == null) {
			defaultValueField = new BaseTextField();
			setFieldEnability(defaultValueField, false);
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
		setFieldEnability(inputMask, false);
		listOptionGroupRole = new ListOptionGroup();
		listOptionGroupRole.setCaption($("AddEditMetadataView.RoleAccess"));

		maxLength = new BaseTextField();
		maxLength.setCaption($("AddEditMetadataView.maxLength"));
		maxLength.setRequired(false);
		maxLength.setId("maxLength");

		maxLength.setVisible(maxLength.isEnabled());
		if (presenter.getMaxLengthFieldValue() != null && !presenter.getMaxLengthFieldValue().equals("")) {
			formMetadataVO.setMaxLength(Integer.parseInt(presenter.getMaxLengthFieldValue()));
			maxLength.setValue(presenter.getMaxLengthFieldValue());
		}

		measurementUnit = new BaseTextField();
		measurementUnit.setCaption($("AddEditMetadataView.measurementUnit"));
		measurementUnit.setRequired(false);
		measurementUnit.setId("measurementUnit");
		measurementUnit.setEnabled(formMetadataVO.getValueType() == MetadataValueType.INTEGER
								   || formMetadataVO.getValueType() == MetadataValueType.NUMBER);
		measurementUnit.setVisible(measurementUnit.isEnabled());
		if (presenter.getMeasurementUnitFieldValue() != null && !presenter.getMeasurementUnitFieldValue().equals("")) {
			measurementUnit.setValue(presenter.getMeasurementUnitFieldValue());
			formMetadataVO.setMeasurementUnit(presenter.getMeasurementUnitFieldValue());
		}

		List<RoleVO> roleList = presenter.getAllCollectionRole();
		listOptionGroupRole.setMultiSelect(true);
		listOptionGroupRole.setImmediate(true);
		List<String> initialSelectedRoles = new ArrayList<>();
		for (RoleVO role : roleList) {
			listOptionGroupRole.addItem(role.getCode());
			listOptionGroupRole.setItemCaption(role.getCode(), role.getTitle());
			for (String roleCode : presenter.getMetadataReadRole()) {
				if (roleCode.equals(role.getCode())) {
					initialSelectedRoles.add(role.getCode());
				}
			}
		}

		listOptionGroupRole.setValue(initialSelectedRoles);
		formMetadataVO.setReadAccessRoles(initialSelectedRoles);

		List<Field<?>> fields = new ArrayList<>(asList((Field<?>) localcodeField, labelsField, dataEntryGroup,
				dataEntryRef, dataEntrySource,  valueType, multivalueType,
				inputType, inputMask, maxLength, measurementUnit, metadataGroup, listOptionGroupRole, refType, requiredField, duplicableField, enabledField, searchableField, sortableField,
				advancedSearchField, highlight, autocomplete, availableInSummary, helpMessagesField, uniqueField, multiLingualField));

		for (CheckBox customAttributeField : customAttributesField) {
			fields.add(customAttributeField);
		}

		fields.add(defaultValueField);
		fields.add(displayType);
		fields.add(sortingType);

		if (parentMetadataLabel != null) {
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
				helpMessagesField.commit();
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

				if (!listOptionGroupRole.isVisible()) {
					formMetadataVO.setReadAccessRoles(new ArrayList<String>());
				}

				try {
					labelsField.validateFields();
					helpMessagesField.validateFields();
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

		if (editMode) {
			isShowUnique = presenter.isShowUniqueComboBox();
		}

		if (!isShowUnique) {
			uniqueField.setContainNonUniqueValueAlready(true);
		}

		if (!formMetadataVO.getLocalcode().toLowerCase().equals("code")) {
			uniqueField.setSomethingOnUnderlyingCheckbox(CheckBox::setEnabled, formMetadataVO.getValueType() == MetadataValueType.STRING && isShowUnique);
			uniqueField.setVisible(formMetadataVO.getValueType() == MetadataValueType.STRING);
		} else {
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

		enableDisableMaxLength();

		return metadataForm;
	}

	public void inputTypeChanged(MetadataInputType metadataInputType) {
		enableDisableMaxLength();
	}

	private void showDuplicateReportDownloadLink(ReportWriter reportWriter, String fileName) {
		BaseWindow window = new BaseWindow();
		window.setContent(new ReportViewer(reportWriter, fileName));
		window.center();
		window.setWidth("50%");
		window.setHeight("30%");

		getUI().addWindow(window);
	}

	private void enableDisableMaxLength() {
		boolean isInherited = isInEditMode();
		MetadataInputType metadataInputType = null;

		if (formMetadataVO != null && isInEditMode()) {
			isInherited = presenter.isInherited(formMetadataVO.getCode());
		}

		if (metadataForm != null) {
			metadataInputType = metadataForm.getViewObject().getInput();
		}

		MetadataValueType metadataValueType = (MetadataValueType) valueType.getValue();
		maxLength.setEnabled(!isInherited && metadataInputType != null && metadataInputType != MetadataInputType.RICHTEXT && (metadataValueType == MetadataValueType.STRING || metadataValueType == MetadataValueType.TEXT));
		maxLength.setVisible(isInherited && metadataInputType != MetadataInputType.RICHTEXT || maxLength.isEnabled());
	}

	@Override
	public void reloadForm() {
		metadataForm.commit();
		metadataForm.reload();
	}

	//TODO Move in presenter
	public void disableFieldsForSystemReservedMetadatas() {

		localcodeField.setReadOnly(true);
		valueType.setReadOnly(true);
		refType.setReadOnly(true);
		inputType.setReadOnly(true);
		multivalueType.setReadOnly(true);
		enabledField.setReadOnly(true);
		//		autocomplete.setReadOnly(true);
		duplicableField.setReadOnly(true);
		inputMask.setReadOnly(true);
		defaultValueField.setReadOnly(true);

		if (!formMetadataVO.getLocalcode().equals(Schemas.LEGACY_ID.getLocalCode())) {
			requiredField.setReadOnly(true);
			searchableField.setReadOnly(true);
			if (!formMetadataVO.getLocalcode().equals(Schemas.PATH.getLocalCode())) {
				advancedSearchField.setReadOnly(true);
			}
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {

		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				List<IntermediateBreadCrumbTailItem> intermediateBreadCrumbTailItemList = BreadcrumbTrailUtil.llistSchemaTypeSchemaList(presenter.getSchemaCode());
				intermediateBreadCrumbTailItemList.add(BreadcrumbTrailUtil
						.editSchemaMetadata(presenter.getParamSchemaCode(), presenter.getSchemaTypeCode(),
								presenter.getSchemaVO().getLabel(getSessionContext().getCurrentLocale().getLanguage())));

				return intermediateBreadCrumbTailItemList;
			}
		};
	}

	private class UniqueFieldLayout extends CustomField<Boolean> {

		private final CheckBox uniqueField;
		private boolean containNonUniqueValueAlready;
		private final Runnable printReport;
		private final HorizontalLayout layout;


		private BaseButton showDuplicateReport;

		private UniqueFieldLayout(Runnable printReport) {
			addStyleName("add-edit-metadata-unique-field-layout");

			this.printReport = printReport;

			uniqueField = new CheckBox();
			uniqueField.setRequired(false);
			uniqueField.setId("unique");

			showDuplicateReport = new BaseButton($("AddEditMetadataView.containNonUniqueValueAlready.displayReportButton")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if (printReport != null) {
						printReport.run();
					}
				}
			};
			showDuplicateReport.addStyleName(ValoTheme.BUTTON_LINK);

			layout = new I18NHorizontalLayout();
			layout.setSpacing(true);

			layout.addComponents(uniqueField, showDuplicateReport);
		}

		@Override
		protected Component initContent() {

			updateUniqueFieldCaption();
			showDuplicateReport.setVisible(isContainingNonUniqueValueAlready());

			return layout;
		}

		@Override
		public Class<? extends Boolean> getType() {
			return Boolean.class;
		}

		@Override
		protected void setInternalValue(Boolean newValue) {
			uniqueField.setValue(newValue);
		}

		@Override
		protected Boolean getInternalValue() {
			return uniqueField.getValue();
		}

		public void setContainNonUniqueValueAlready(boolean containNonUniqueValueAlready) {
			this.containNonUniqueValueAlready = containNonUniqueValueAlready;

			updateUniqueFieldCaption();
			showDuplicateReport.setVisible(containNonUniqueValueAlready);
		}

		public boolean isContainingNonUniqueValueAlready() {
			return containNonUniqueValueAlready;
		}

		public <T> UniqueFieldLayout setSomethingOnUnderlyingCheckbox(BiConsumer<CheckBox, T> setter, T value) {
			if (setter != null) {
				setter.accept(uniqueField, value);
			}

			return this;
		}

		private void updateUniqueFieldCaption() {
			uniqueField.setCaption($("AddEditMetadataView.unique") + (isContainingNonUniqueValueAlready() ? " " + $("AddEditMetadataView.containNonUniqueValueAlready") : ""));
		}
	}
}
