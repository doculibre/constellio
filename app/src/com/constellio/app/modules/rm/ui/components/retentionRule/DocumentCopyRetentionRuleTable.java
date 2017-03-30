package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.ui.components.converters.MediumTypeIdListToCodesConverter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.MetadataCodeToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdListToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;

public class DocumentCopyRetentionRuleTable extends CustomField<List<CopyRetentionRule>> {
	
	private static final String CODE = "code";
	private static final String DETAILS = "details";
	private static final String DOCUMENT_TYPE = "typeId";
	private static final String MEDIUM_TYPES = "mediumTypeIds";
	private static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	private static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
	private static final String ACTIVE_RETENTION_TITLE = "activeRetentionTitle";
	private static final String ACTIVE_RETENTION_COMMENT = "activeRetentionComment";
	private static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
	private static final String SEMI_ACTIVE_RETENTION_COMMENT = "semiActiveRetentionComment";
	private static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	private static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
	private static final String DELETE_BUTTON = "deleteButton";
	
	private final List<VariableRetentionPeriodVO> variableRetentionPeriodVOList;

	private RecordIdToCaptionConverter documentTypeConverter = new RecordIdToCaptionConverter();
	private MediumTypeIdListToCodesConverter mediumTypeIdListToCodesConverter = new MediumTypeIdListToCodesConverter();
//	private EnumWithSmallCodeToCaptionConverter disposalTypeConverter = new EnumWithSmallCodeToCaptionConverter(
//			DisposalType.class);
	private MetadataCodeToStringConverter metadataCodeToStringConverter = new MetadataCodeToStringConverter();
	private RetentionRuleVO retentionRuleVO;
	private VerticalLayout mainLayout;
	private AddButton addButton;
	private Table table;
	private Table variablePeriodTable;
	private RetentionRuleTablePresenter presenter;
	private boolean formMode;

	public DocumentCopyRetentionRuleTable(RetentionRuleVO retentionRuleVO, boolean formMode,
			final RetentionRuleTablePresenter presenter) {
		this.retentionRuleVO = retentionRuleVO;
		this.formMode = formMode;
		this.presenter = presenter;
		this.variableRetentionPeriodVOList = presenter.getOpenPeriodsDDVList();

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		if (formMode) {
			addButton = new AddButton($("DocumentCopyRetentionRuleListTable.addCopy")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					CopyRetentionRule newCopy = presenter.newDocumentCopyRetentionRule();
					List<CopyRetentionRule> copyRetentionRules = getCopyRetentionRules();
					copyRetentionRules.add(newCopy);
					addItems();
				}
			};
		}

		table = new Table();
		table.setWidth("100%");
		table.setPageLength(0);

		table.setColumnHeader(CODE, $("FolderCopyRetentionRuleListTable.code"));
		table.setColumnHeader(DETAILS, $("FolderCopyRetentionRuleListTable.details"));
		table.setColumnHeader(DOCUMENT_TYPE, $("DocumentCopyRetentionRuleListTable.documentTypeId"));
		table.setColumnHeader(MEDIUM_TYPES, $("DocumentCopyRetentionRuleListTable.mediumTypes"));
		table.setColumnHeader(CONTENT_TYPES_COMMENT, "");
		table.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("DocumentCopyRetentionRuleListTable.active"));
		table.setColumnHeader(ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_PERIOD, $("DocumentCopyRetentionRuleListTable.semiActive"));
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(INACTIVE_DISPOSAL_TYPE, $("DocumentCopyRetentionRuleListTable.inactive"));
		table.setColumnHeader(INACTIVE_DISPOSAL_COMMENT, "");
		table.setColumnHeader(DELETE_BUTTON, "");

		if (formMode) {
			table.addContainerProperty(CODE, BaseTextField.class, null);
			table.addContainerProperty(DETAILS, DetailsFieldGroup.class, null);
			table.addContainerProperty(DOCUMENT_TYPE, LookupRecordField.class, null);
			table.addContainerProperty(MEDIUM_TYPES, MediumTypesField.class, null);
			table.addContainerProperty(CONTENT_TYPES_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_PERIOD, ActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_PERIOD, SemiActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_TYPE, InactiveDisposalTypeField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(DELETE_BUTTON, Button.class, null);
		} else {
			table.addContainerProperty(CODE, Label.class, null);
			table.addContainerProperty(DOCUMENT_TYPE, Label.class, null);
			table.addContainerProperty(MEDIUM_TYPES, Label.class, null);
			table.addContainerProperty(CONTENT_TYPES_COMMENT, Label.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_PERIOD, ActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_COMMENT, Label.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_PERIOD, SemiActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_COMMENT, Label.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_TYPE, Label.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_COMMENT, Label.class, null);
		}

		table.addValueChangeListener(new ValueChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				List<CopyRetentionRule> newValue = (List<CopyRetentionRule>) table.getItemIds();
				DocumentCopyRetentionRuleTable.this.retentionRuleVO.setDocumentCopyRetentionRules(newValue);
			}
		});

		if (addButton != null) {
			mainLayout.addComponent(addButton);
			mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		}
		mainLayout.addComponents(table, buildVariablePeriodTable());

		addItems();
	}

	@SuppressWarnings("unchecked")
	private Table buildVariablePeriodTable() {
		variablePeriodTable = new Table();
		variablePeriodTable.setWidth("100%");
		variablePeriodTable.setPageLength(Math.min(15, variableRetentionPeriodVOList.size()));
		variablePeriodTable.setVisible(!formMode && !variableRetentionPeriodVOList.isEmpty());

		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("DocumentCopyRetentionRuleListTable.active.code"));
		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_TITLE, $("DocumentCopyRetentionRuleListTable.active.title"));

		variablePeriodTable.addContainerProperty(ACTIVE_RETENTION_PERIOD, Label.class, null);
		variablePeriodTable.addContainerProperty(ACTIVE_RETENTION_TITLE, Label.class, null);

		for (VariableRetentionPeriodVO variableRetentionPeriodVO : variableRetentionPeriodVOList) {
			variablePeriodTable.addItem(variableRetentionPeriodVO);
			Label activeRetentionCodeLabel = new Label("" + variableRetentionPeriodVO.getCode());
			Label activeRetentionTitleLabel = new Label("" + variableRetentionPeriodVO.getTitle());
			variablePeriodTable.getContainerProperty(variableRetentionPeriodVO, ACTIVE_RETENTION_PERIOD)
					.setValue(activeRetentionCodeLabel);
			variablePeriodTable.getContainerProperty(variableRetentionPeriodVO, ACTIVE_RETENTION_TITLE)
					.setValue(activeRetentionTitleLabel);
		}
		return variablePeriodTable;
	}

	protected void onDisposalTypeChange(CopyRetentionRule rule) {
	}

	protected List<MetadataVO> getDateMetadataVOs(String documentTypeId) {
		return new ArrayList<>();
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

	private List<CopyRetentionRule> getCopyRetentionRules() {
		List<CopyRetentionRule> copyRetentionRules = retentionRuleVO.getDocumentCopyRetentionRules();
		if (copyRetentionRules == null) {
			copyRetentionRules = new ArrayList<>();
			retentionRuleVO.setDocumentCopyRetentionRules(copyRetentionRules);
		}
		return copyRetentionRules;
	}

	@SuppressWarnings("unchecked")
	private void addItem(final CopyRetentionRule copyRetentionRule) {
		table.addItem(copyRetentionRule);

		if (formMode) {
			MiniTextField codeField = new MiniTextField();

			LookupRecordField documentTypeField = new LookupRecordField(DocumentType.SCHEMA_TYPE);
			documentTypeField.setRequired(true);
			final DetailsFieldGroup detailsField = new DetailsFieldGroup(copyRetentionRule);
			final ActiveRetentionPeriodFieldGroup activeRetentionPeriodFieldGroup = new ActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			MediumTypesField mediumTypesField = new MediumTypesField(copyRetentionRule);
			MiniTextField contentTypesCommentField = new MiniTextField();
			MiniTextField activeRetentionCommentField = new MiniTextField();
			final SemiActiveRetentionPeriodFieldGroup semiActiveRetentionPeriodFieldGroup = new SemiActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			MiniTextField semiActiveRetentionCommentField = new MiniTextField();
			InactiveDisposalTypeField inactiveDisposalTypeField = new InactiveDisposalTypeField(copyRetentionRule);
			MiniTextField inactiveDisposalCommentField = new MiniTextField();

			codeField.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
			documentTypeField.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, DOCUMENT_TYPE));
			activeRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, ACTIVE_RETENTION_COMMENT));
			semiActiveRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT));
			inactiveDisposalCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT));

			table.getContainerProperty(copyRetentionRule, CODE).setValue(codeField);
			table.getContainerProperty(copyRetentionRule, DETAILS).setValue(detailsField);
			table.getContainerProperty(copyRetentionRule, DOCUMENT_TYPE).setValue(documentTypeField);
			table.getContainerProperty(copyRetentionRule, MEDIUM_TYPES).setValue(mediumTypesField);
			table.getContainerProperty(copyRetentionRule, CONTENT_TYPES_COMMENT).setValue(contentTypesCommentField);
			table.getContainerProperty(copyRetentionRule, ACTIVE_RETENTION_PERIOD).setValue(activeRetentionPeriodFieldGroup);
			table.getContainerProperty(copyRetentionRule, ACTIVE_RETENTION_COMMENT).setValue(activeRetentionCommentField);
			table.getContainerProperty(copyRetentionRule, SEMI_ACTIVE_RETENTION_PERIOD)
					.setValue(semiActiveRetentionPeriodFieldGroup);
			table.getContainerProperty(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT)
					.setValue(semiActiveRetentionCommentField);
			table.getContainerProperty(copyRetentionRule, INACTIVE_DISPOSAL_TYPE).setValue(inactiveDisposalTypeField);
			table.getContainerProperty(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT).setValue(inactiveDisposalCommentField);

			Button removeButton = new DeleteButton() {
				@Override
				protected void confirmButtonClick(ConfirmDialog dialog) {
					getCopyRetentionRules().remove(copyRetentionRule);
					addItems();
				}
			};
			table.getContainerProperty(copyRetentionRule, DELETE_BUTTON).setValue(removeButton);

			documentTypeField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					copyRetentionRule.setActiveDateMetadata(null);
					copyRetentionRule.setSemiActiveDateMetadata(null);
					activeRetentionPeriodFieldGroup.getDateMetadataField().addOptions();
					semiActiveRetentionPeriodFieldGroup.getDateMetadataField().addOptions();
				}
			});
		} else {
			Label codeLabel = new Label();
			Label documentTypeLabel = new Label();
			Label mediumTypesLabel = new Label();
			Label contentTypesCommentLabel = new Label();
			ActiveRetentionPeriodFieldGroup activeRetentionPeriodFieldGroup = new ActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			Label activeRetentionCommentLabel = new Label();
			SemiActiveRetentionPeriodFieldGroup semiActiveRetentionPeriodFieldGroup = new SemiActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			Label semiActiveRetentionCommentLabel = new Label();
			Label inactiveDisposalTypeLabel = new Label();
			Label inactiveDisposalCommentLabel = new Label();

			documentTypeLabel.setConverter(documentTypeConverter);
			mediumTypesLabel.setConverter(mediumTypeIdListToCodesConverter);
//			inactiveDisposalTypeLabel.setConverter(disposalTypeConverter);

			codeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
			documentTypeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, DOCUMENT_TYPE));
			mediumTypesLabel.setPropertyDataSource(new NestedMethodProperty<List<String>>(copyRetentionRule, MEDIUM_TYPES));
			contentTypesCommentLabel
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CONTENT_TYPES_COMMENT));
			activeRetentionCommentLabel
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, ACTIVE_RETENTION_COMMENT));
			semiActiveRetentionCommentLabel
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT));
			inactiveDisposalTypeLabel
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_TYPE + ".code"));
			inactiveDisposalCommentLabel
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT));

			table.getContainerProperty(copyRetentionRule, CODE).setValue(codeLabel);
			table.getContainerProperty(copyRetentionRule, DOCUMENT_TYPE).setValue(documentTypeLabel);
			table.getContainerProperty(copyRetentionRule, MEDIUM_TYPES).setValue(mediumTypesLabel);
			table.getContainerProperty(copyRetentionRule, CONTENT_TYPES_COMMENT).setValue(contentTypesCommentLabel);
			table.getContainerProperty(copyRetentionRule, ACTIVE_RETENTION_PERIOD).setValue(activeRetentionPeriodFieldGroup);
			table.getContainerProperty(copyRetentionRule, ACTIVE_RETENTION_COMMENT).setValue(activeRetentionCommentLabel);
			table.getContainerProperty(copyRetentionRule, SEMI_ACTIVE_RETENTION_PERIOD)
					.setValue(semiActiveRetentionPeriodFieldGroup);
			table.getContainerProperty(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT)
					.setValue(semiActiveRetentionCommentLabel);
			table.getContainerProperty(copyRetentionRule, INACTIVE_DISPOSAL_TYPE).setValue(inactiveDisposalTypeLabel);
			table.getContainerProperty(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT).setValue(inactiveDisposalCommentLabel);
		}
	}

	private void addItems() {
		table.removeAllItems();
		List<CopyRetentionRule> copyRetentionRules = getCopyRetentionRules();
		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			addItem(copyRetentionRule);
		}
	}

	@Override
	public List<CopyRetentionRule> getValue() {
		return retentionRuleVO.getDocumentCopyRetentionRules();
	}

	@Override
	public void setValue(List<CopyRetentionRule> newFieldValue)
			throws ReadOnlyException, ConversionException {
		retentionRuleVO.setDocumentCopyRetentionRules(newFieldValue);
		addItems();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getType() {
		return List.class;
	}

	private class MiniTextField extends BaseTextField {

		public MiniTextField() {
			setWidth("40px");
		}

	}

	private class MediumTypesField extends ListAddRemoveRecordComboBox {

		public MediumTypesField(CopyRetentionRule copyRetentionRule) {
			super(MediumType.DEFAULT_SCHEMA);
			setPropertyDataSource(new NestedMethodProperty<>(copyRetentionRule, MEDIUM_TYPES));
			setBuffered(false);
			addStyleName("copy-retention-rule-table-medium-types-field");
			setWidth("100%");
		}

		@Override
		protected boolean isEditPossible() {
			return false;
		}

	}

	private class RetentionPeriodFieldGroup extends VerticalLayout {

		private MetadataField dateMetadataField;

		public RetentionPeriodFieldGroup(final CopyRetentionRule copyRetentionRule, final boolean activeRetentionPeriod) {
			setSpacing(true);

			RetentionPeriod retentionPeriod = (activeRetentionPeriod) ?
					copyRetentionRule.getActiveRetentionPeriod() :
					copyRetentionRule.getSemiActiveRetentionPeriod();

			Property<String> dateMetadataProperty;
			if (activeRetentionPeriod) {
				dateMetadataProperty = new MethodProperty<>(copyRetentionRule, "activeDateMetadata");
			} else {
				dateMetadataProperty = new MethodProperty<>(copyRetentionRule, "semiActiveDateMetadata");
			}
			if (formMode) {
				dateMetadataField = new MetadataField(copyRetentionRule);
				dateMetadataField.setPropertyDataSource(dateMetadataProperty);

				BeanItemContainer<VariableRetentionPeriodVO> container = new BeanItemContainer<>(VariableRetentionPeriodVO.class,
						getVariablePeriods());
				final ComboBox openRetentionPeriodDDVField = new ComboBox("", container);
				openRetentionPeriodDDVField.setInputPrompt($("fixedPeriod"));
				openRetentionPeriodDDVField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
				for (VariableRetentionPeriodVO periodVO : container.getItemIds()) {
					openRetentionPeriodDDVField.setItemCaption(periodVO, periodVO.getCode() + " - " + periodVO.getTitle());
				}

				final MiniTextField yearsField = new MiniTextField();
				yearsField.setConverter(new StringToIntegerConverter());

				if (retentionPeriod.getRetentionType() == RetentionType.FIXED) {
					openRetentionPeriodDDVField.setValue(null);
					yearsField.setConvertedValue(retentionPeriod.getValue());
					yearsField.setEnabled(true);
				} else {
					for (VariableRetentionPeriodVO periodVO : container.getItemIds()) {
						if (periodVO.getCode().equals(retentionPeriod.getVariablePeriodCode())) {
							openRetentionPeriodDDVField.setValue(periodVO);
							break;
						}
					}
					if (activeRetentionPeriod && retentionPeriod.getRetentionType() == RetentionType.OPEN) {
						yearsField.setConvertedValue(copyRetentionRule.getOpenActiveRetentionPeriod());
						yearsField.setEnabled(true);
					} else {
						yearsField.setEnabled(false);
					}
				}

				openRetentionPeriodDDVField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						RetentionPeriod newRetentionPeriod;
						VariableRetentionPeriodVO newValue = (VariableRetentionPeriodVO) openRetentionPeriodDDVField.getValue();
						if (newValue != null) {
							if (activeRetentionPeriod) {
								copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.variable(newValue.getCode()));
								newRetentionPeriod = copyRetentionRule.getActiveRetentionPeriod();
							} else {
								copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.variable(newValue.getCode()));
								newRetentionPeriod = copyRetentionRule.getSemiActiveRetentionPeriod();
							}
							yearsField.setValue(null);
							yearsField.setEnabled(activeRetentionPeriod &&
									newRetentionPeriod.getRetentionType() == RetentionType.OPEN);
						} else {
							yearsField.setEnabled(true);
						}
					}
				});

				yearsField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						try {
							yearsField.validate();
							Integer newValue = (Integer) yearsField.getConvertedValue();
							RetentionPeriod period = activeRetentionPeriod ?
									copyRetentionRule.getActiveRetentionPeriod() :
									copyRetentionRule.getSemiActiveRetentionPeriod();
							if (activeRetentionPeriod && period.getRetentionType() == RetentionType.OPEN) {
								copyRetentionRule.setOpenActiveRetentionPeriod(newValue);
							} else if (newValue != null) {
								openRetentionPeriodDDVField.setValue(null);
								if (activeRetentionPeriod) {
									copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(newValue));
								} else {
									copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(newValue));
								}
							}
						} catch (InvalidValueException e) {
							// Invalid value
						}
					}
				});

				addComponents(dateMetadataField, openRetentionPeriodDDVField, yearsField);
			} else {
				String metadataCode;
				if (activeRetentionPeriod) {
					metadataCode = copyRetentionRule.getActiveDateMetadata();
				} else {
					metadataCode = copyRetentionRule.getSemiActiveDateMetadata();
				}
				String metadataLabelStr = metadataCodeToStringConverter
						.convertToPresentation(metadataCode, String.class,
								ConstellioUI.getCurrent().getSessionContext().getCurrentLocale());
				Label metadataLabel = new Label(metadataLabelStr);
				Label retentionPeriodLabel = new Label("" + retentionPeriod.getValue());
				addComponents(metadataLabel, retentionPeriodLabel);
			}
		}

		public MetadataField getDateMetadataField() {
			return dateMetadataField;
		}

	}

	private class DetailsFieldGroup extends VerticalLayout {

		private BaseTextField titleField;
		private BaseTextArea descriptionField;
		private CheckBox ignoreActivePeriodField;

		public DetailsFieldGroup(final CopyRetentionRule copyRetentionRule) {
			final WindowButton windowButton = new WindowButton($("DetailsFieldGroup.detailsButton"),$("DetailsFieldGroup.detailsWindow")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout windowLayout = new VerticalLayout();

					windowLayout.setSpacing(true);

					Property<String> titleProperty = new MethodProperty<>(copyRetentionRule, "title");
					Property<String> descriptionProperty = new MethodProperty<>(copyRetentionRule, "description");
					Property<Boolean> ignoreActivePeriodProperty = new MethodProperty<>(copyRetentionRule,"ignoreActivePeriod");

					titleField = new BaseTextField($("DetailsFieldGroup.title"), titleProperty);
					titleField.setWidth("90%");
					descriptionField = new BaseTextArea($("DetailsFieldGroup.description"), descriptionProperty);
					descriptionField.setWidth("90%");
					ignoreActivePeriodField = new CheckBox($("DetailsFieldGroup.ignoreActivePeriod"), ignoreActivePeriodProperty);

					Button closeButton = new BaseButton("OK") {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};

					windowLayout.addComponents(titleField, descriptionField,ignoreActivePeriodField,closeButton);
					return windowLayout;
				}
			};
			addComponent(windowButton);
		}

		public BaseTextField getTitleField() {
			return titleField;
		}

		public BaseTextArea getDescriptionField() {
			return descriptionField;
		}

	}

	private Collection<VariableRetentionPeriodVO> getVariablePeriods() {
		return variableRetentionPeriodVOList;
		/*ArrayList<DDVVO> returnList = new ArrayList<>();
		returnList.add(new DDVVO().setTitle("1Title").setCode("1Code"));
		returnList.add(new DDVVO().setTitle("2Title").setCode("2Code"));
		return returnList;*/
	}

	private class ActiveRetentionPeriodFieldGroup extends RetentionPeriodFieldGroup {

		public ActiveRetentionPeriodFieldGroup(CopyRetentionRule copyRetentionRule) {
			super(copyRetentionRule, true);
		}

	}

	private class SemiActiveRetentionPeriodFieldGroup extends RetentionPeriodFieldGroup {

		public SemiActiveRetentionPeriodFieldGroup(CopyRetentionRule copyRetentionRule) {
			super(copyRetentionRule, false);
		}

	}

	private class InactiveDisposalTypeField extends EnumWithSmallCodeComboBox<DisposalType> {

		public InactiveDisposalTypeField(final CopyRetentionRule copyRetentionRule) {
			super(DisposalType.class);
			setPropertyDataSource(new NestedMethodProperty<>(copyRetentionRule, INACTIVE_DISPOSAL_TYPE));

			addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					onDisposalTypeChange(copyRetentionRule);
				}
			});
		}

	}

	private class MetadataField extends BaseComboBox {

		private CopyRetentionRule copyRetentionRule;

		public MetadataField(CopyRetentionRule copyRetentionRule) {
			super();
			this.copyRetentionRule = copyRetentionRule;
			addOptions();
		}

		protected void addOptions() {
			removeAllItems();

			String documentTypeId = copyRetentionRule.getTypeId();
			List<MetadataVO> dateMetadataVOs = getDateMetadataVOs(documentTypeId);

			Locale locale = VaadinSession.getCurrent().getLocale();
			for (MetadataVO metadataVO : dateMetadataVOs) {
				String metatadaCode = metadataVO.getCode();
				addItem(metatadaCode);
				setItemCaption(metatadaCode, metadataVO.getLabel(locale));
			}
		}
	}
}
