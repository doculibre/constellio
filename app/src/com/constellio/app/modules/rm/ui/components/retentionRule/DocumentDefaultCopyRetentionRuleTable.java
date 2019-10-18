package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.converters.BaseStringToIntegerConverter;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.MetadataCodeToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdListToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentDefaultCopyRetentionRuleTable extends CustomComponent {
	private static final String COPY_TYPE = "copyType";
	private static final String DETAILS = "details";
	private static final String MEDIUM_TYPES = "mediumTypeIds";
	private static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	private static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
	private static final String ACTIVE_RETENTION_TITLE = "activeRetentionTitle";
	private static final String ACTIVE_RETENTION_COMMENT = "activeRetentionComment";
	private static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
	private static final String SEMI_ACTIVE_RETENTION_COMMENT = "semiActiveRetentionComment";
	private static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	private static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
	private final List<VariableRetentionPeriodVO> variableRetentionPeriodVOList;

	private RetentionRuleTablePresenter presenter;
	private EnumWithSmallCodeToCaptionConverter copyTypeConverter = new EnumWithSmallCodeToCaptionConverter(CopyType.class);
	private RecordIdListToStringConverter recordIdListToStringConverter = new RecordIdListToStringConverter();
	private EnumWithSmallCodeToCaptionConverter disposalTypeConverter = new EnumWithSmallCodeToCaptionConverter(
			DisposalType.class);
	private MetadataCodeToStringConverter metadataCodeToStringConverter = new MetadataCodeToStringConverter();
	private RetentionRuleVO retentionRuleVO;
	private List<MetadataVO> dateMetadataVOs;
	private VerticalLayout mainLayout;
	private Table table;
	private Table variablePeriodTable;
	private boolean formMode;

	private Field<CopyRetentionRule> principalCopyField;
	private Field<CopyRetentionRule> secondaryCopyField;

	public DocumentDefaultCopyRetentionRuleTable(RetentionRuleVO retentionRuleVO, boolean formMode,
												 RetentionRuleTablePresenter presenter) {
		this.retentionRuleVO = retentionRuleVO;
		this.formMode = formMode;
		this.dateMetadataVOs = getDateMetadataVOs();
		this.presenter = presenter;
		this.variableRetentionPeriodVOList = presenter.getOpenPeriodsDDVList();

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		table = new BaseTable(getClass().getName());
		table.addStyleName("copy-retention-rule-table");
		table.setWidth("100%");
		table.setPageLength(0);

		table.setColumnHeader(COPY_TYPE, $("DocumentDefaultCopyRetentionRuleListTable.copyType"));
		table.setColumnHeader(DETAILS, $("FolderCopyRetentionRuleListTable.details"));
		table.setColumnHeader(MEDIUM_TYPES, $("DocumentDefaultCopyRetentionRuleListTable.mediumTypes"));
		table.setColumnHeader(CONTENT_TYPES_COMMENT, "");
		table.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("DocumentDefaultCopyRetentionRuleListTable.active"));
		table.setColumnHeader(ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_PERIOD, $("DocumentDefaultCopyRetentionRuleListTable.semiActive"));
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(INACTIVE_DISPOSAL_TYPE, $("DocumentDefaultCopyRetentionRuleListTable.inactive"));
		table.setColumnHeader(INACTIVE_DISPOSAL_COMMENT, "");

		if (formMode) {
			table.addContainerProperty(COPY_TYPE, Label.class, null);
			table.addContainerProperty(DETAILS, DetailsFieldGroup.class, null);
			table.addContainerProperty(MEDIUM_TYPES, MediumTypesField.class, null);
			table.addContainerProperty(CONTENT_TYPES_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_PERIOD, ActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_PERIOD, SemiActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_TYPE, InactiveDisposalTypeField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_COMMENT, MiniTextField.class, null);
		} else {
			table.addContainerProperty(COPY_TYPE, Label.class, null);
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
				DocumentDefaultCopyRetentionRuleTable.this.retentionRuleVO.setDocumentCopyRetentionRules(newValue);
			}
		});

		mainLayout.addComponents(table, buildVariablePeriodTable());

		addItems();
		initFields();

		setCompositionRoot(mainLayout);
	}

	@SuppressWarnings("unchecked")
	private Table buildVariablePeriodTable() {
		variablePeriodTable = new BaseTable(getClass().getName() + ".variablePeriodTable");
		variablePeriodTable.setWidth("100%");
		variablePeriodTable.setPageLength(Math.min(15, variableRetentionPeriodVOList.size()));
		variablePeriodTable.setVisible(!formMode && !variableRetentionPeriodVOList.isEmpty());

		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("DocumentDefaultCopyRetentionRuleListTable.active.code"));
		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_TITLE, $("DocumentDefaultCopyRetentionRuleListTable.active.title"));

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

	protected List<MetadataVO> getDateMetadataVOs() {
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private void addItem(final CopyRetentionRule copyRetentionRule) {
		table.addItem(copyRetentionRule);

		Label copyTypeLabel = new Label();
		copyTypeLabel.setConverter(copyTypeConverter);
		copyTypeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, COPY_TYPE + ".code"));

		if (formMode) {
			ActiveRetentionPeriodFieldGroup activeRetentionPeriodFieldGroup = new ActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			final DetailsFieldGroup detailsField = new DetailsFieldGroup(copyRetentionRule);
			MediumTypesField mediumTypesField = new MediumTypesField(copyRetentionRule);
			MiniTextField contentTypesCommentField = new MiniTextField();
			MiniTextField activeRetentionCommentField = new MiniTextField();
			SemiActiveRetentionPeriodFieldGroup semiActiveRetentionPeriodFieldGroup = new SemiActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			MiniTextField semiActiveRetentionCommentField = new MiniTextField();
			InactiveDisposalTypeField inactiveDisposalTypeField = new InactiveDisposalTypeField(copyRetentionRule);
			MiniTextField inactiveDisposalCommentField = new MiniTextField();

			activeRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, ACTIVE_RETENTION_COMMENT));
			semiActiveRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT));
			inactiveDisposalCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT));

			table.getContainerProperty(copyRetentionRule, COPY_TYPE).setValue(copyTypeLabel);
			table.getContainerProperty(copyRetentionRule, DETAILS).setValue(detailsField);
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

			if (copyRetentionRule.getCopyType() == CopyType.SECONDARY) {
				inactiveDisposalTypeField.setEnabled(false);
			}
		} else {
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

			mediumTypesLabel.setConverter(recordIdListToStringConverter);
			inactiveDisposalTypeLabel.setConverter(disposalTypeConverter);

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

			table.getContainerProperty(copyRetentionRule, COPY_TYPE).setValue(copyTypeLabel);
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

		if (retentionRuleVO.getScope() == RetentionRuleScope.DOCUMENTS) {
			CopyRetentionRule principalCopyRetentionRule = retentionRuleVO.getPrincipalDefaultDocumentCopyRetentionRule();
			if (principalCopyRetentionRule == null) {
				principalCopyRetentionRule = presenter.newDocumentDefaultCopyRetentionRule(true);
				retentionRuleVO.setPrincipalDefaultDocumentCopyRetentionRule(principalCopyRetentionRule);
			}

			CopyRetentionRule secondaryCopyRetentionRule = retentionRuleVO.getSecondaryDefaultDocumentCopyRetentionRule();
			if (secondaryCopyRetentionRule == null) {
				secondaryCopyRetentionRule = presenter.newDocumentDefaultCopyRetentionRule(false);
				retentionRuleVO.setSecondaryDefaultDocumentCopyRetentionRule(secondaryCopyRetentionRule);
			}

			addItem(principalCopyRetentionRule);
			addItem(secondaryCopyRetentionRule);
		}
	}

	private void initFields() {
		principalCopyField = initField(true);
		secondaryCopyField = initField(false);
	}

	private Field<CopyRetentionRule> initField(final Boolean principal) {
		return new CustomField<CopyRetentionRule>() {
			@Override
			public Class<? extends CopyRetentionRule> getType() {
				return CopyRetentionRule.class;
			}

			@Override
			protected Component initContent() {
				Component content;
				if (principal) {
					content = DocumentDefaultCopyRetentionRuleTable.this;
				} else {
					content = new CustomComponent();
				}
				return content;
			}
		};
	}

	public Field<CopyRetentionRule> getPrincipalCopyRetentionRuleField() {
		return principalCopyField;
	}

	public Field<CopyRetentionRule> getSecondaryCopyRetentionRuleField() {
		return secondaryCopyField;
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

		public RetentionPeriodFieldGroup(final CopyRetentionRule copyRetentionRule,
										 final boolean activeRetentionPeriod) {
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
				ComboBox dateMetadataField = new MetadataField(dateMetadataVOs);
				dateMetadataField.setPropertyDataSource(dateMetadataProperty);

				BeanItemContainer<VariableRetentionPeriodVO> container = new BeanItemContainer<>(VariableRetentionPeriodVO.class,
						getVariablePeriods());
				final ComboBox openRetentionPeriodDDVField = new BaseComboBox("", container);
				openRetentionPeriodDDVField.setInputPrompt($("fixedPeriod"));
				openRetentionPeriodDDVField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
				for (VariableRetentionPeriodVO periodVO : container.getItemIds()) {
					openRetentionPeriodDDVField.setItemCaption(periodVO, periodVO.getCode() + " - " + periodVO.getTitle());
				}

				final MiniTextField yearsField = new MiniTextField();
				yearsField.setConverter(new BaseStringToIntegerConverter());

				if (retentionPeriod.getRetentionType() == RetentionType.FIXED) {
					openRetentionPeriodDDVField.setValue(null);
					yearsField.setConvertedValue(retentionPeriod.getValue());
					yearsField.setEnabled(true);
				} else {
					for (VariableRetentionPeriodVO periodVO : container.getItemIds()) {
						if (periodVO.getCode().equals(retentionPeriod.getVariablePeriodCode())) {
							openRetentionPeriodDDVField.setValue(periodVO);
							openRetentionPeriodDDVField
									.setItemCaption(periodVO, periodVO.getCode() + " - " + periodVO.getTitle());
							break;
						}
					}
					yearsField.setEnabled(false);
				}

				openRetentionPeriodDDVField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						VariableRetentionPeriodVO newValue = (VariableRetentionPeriodVO) openRetentionPeriodDDVField.getValue();
						if (newValue != null) {
							yearsField.setValue(null);
							yearsField.setEnabled(false);
							if (activeRetentionPeriod) {
								copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.variable(newValue.getCode()));
							} else {
								copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.variable(newValue.getCode()));
							}
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
							if (newValue != null) {
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
						.convertToPresentation(metadataCode, String.class, getLocale());
				Label metadataLabel = new Label(metadataLabelStr);
				Label retentionPeriodLabel = new Label("" + retentionPeriod.getValue());
				addComponents(metadataLabel, retentionPeriodLabel);
			}
		}

	}

	private class DetailsFieldGroup extends VerticalLayout {

		private BaseTextField titleField;
		private BaseTextArea descriptionField;
		private CheckBox ignoreActivePeriodField;
		private ComboBox semiActiveYearTypeField;
		private ComboBox inactiveYearTypeField;

		public DetailsFieldGroup(final CopyRetentionRule copyRetentionRule) {
			WindowConfiguration windowConfiguration = WindowConfiguration.modalDialog("50%", "550px");
			final WindowButton windowButton = new WindowButton($("DetailsFieldGroup.detailsButton"),
					$("DetailsFieldGroup.detailsWindow"), windowConfiguration) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout windowLayout = new VerticalLayout();

					windowLayout.setSpacing(true);
					Property<String> titleProperty = new MethodProperty<>(copyRetentionRule, "title");
					Property<String> descriptionProperty = new MethodProperty<>(copyRetentionRule, "description");
					Property<Boolean> ignoreActivePeriodProperty = new MethodProperty<>(copyRetentionRule, "ignoreActivePeriod");
					Property<Boolean> semiActiveYearTypeProperty = new MethodProperty<>(copyRetentionRule,
							"semiActiveYearTypeId");
					Property<Boolean> inactiveYearTypeProperty = new MethodProperty<>(copyRetentionRule, "inactiveYearTypeId");

					titleField = new BaseTextField($("DetailsFieldGroup.title"), titleProperty);
					titleField.setWidth("90%");
					descriptionField = new BaseTextArea($("DetailsFieldGroup.description"), descriptionProperty);
					descriptionField.setWidth("90%");
					ignoreActivePeriodField = new CheckBox($("DetailsFieldGroup.ignoreActivePeriod"), ignoreActivePeriodProperty);

					semiActiveYearTypeField = new RecordComboBox(YearType.DEFAULT_SCHEMA);
					semiActiveYearTypeField.setCaption($("DetailsFieldGroup.semiActiveYearType"));
					semiActiveYearTypeField.setPropertyDataSource(semiActiveYearTypeProperty);

					inactiveYearTypeField = new RecordComboBox(YearType.DEFAULT_SCHEMA);
					inactiveYearTypeField.setCaption($("DetailsFieldGroup.inactiveYearType"));
					inactiveYearTypeField.setPropertyDataSource(inactiveYearTypeProperty);

					Button closeButton = new BaseButton($("Ok")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};
					closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

					windowLayout.addComponents(titleField, descriptionField, ignoreActivePeriodField, semiActiveYearTypeField,
							inactiveYearTypeField, closeButton);
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

		public MetadataField(List<MetadataVO> options) {
			super();
			Locale locale = VaadinSession.getCurrent().getLocale();
			for (MetadataVO metadataVO : options) {
				String metatadaCode = metadataVO.getCode();
				addItem(metatadaCode);
				setItemCaption(metatadaCode, metadataVO.getLabel(locale));
			}
		}

	}

}
