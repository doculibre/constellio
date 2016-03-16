package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.MetadataCodeToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdListToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class FolderCopyRetentionRuleTable extends CustomField<List<CopyRetentionRule>> {
	private static final String CODE = "code";
	private static final String COPY_TYPE = "copyType";
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

	private EnumWithSmallCodeToCaptionConverter copyTypeConverter = new EnumWithSmallCodeToCaptionConverter(CopyType.class);
	private RecordIdListToStringConverter recordIdListToStringConverter = new RecordIdListToStringConverter();
	private EnumWithSmallCodeToCaptionConverter disposalTypeConverter = new EnumWithSmallCodeToCaptionConverter(
			DisposalType.class);
	private MetadataCodeToStringConverter metadataCodeToStringConverter = new MetadataCodeToStringConverter();
	private RetentionRuleVO retentionRuleVO;
	private VerticalLayout mainLayout;
	private AddButton addButton;
	private Table table;
	private Table variablePeriodTable;
	private RetentionRuleTablePresenter presenter;
	private boolean formMode;

	public FolderCopyRetentionRuleTable(RetentionRuleVO retentionRuleVO, boolean formMode,
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
			addButton = new AddButton($("FolderCopyRetentionRuleListTable.addPrincipalCopy")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					CopyRetentionRule newCopy = presenter.newFolderCopyRetentionRule(true);
					List<CopyRetentionRule> copyRetentionRules = getCopyRetentionRules();
					int indexOfNewCopy;
					if (copyRetentionRules.size() > 1) {
						indexOfNewCopy = copyRetentionRules.size() - 1;
					} else {
						indexOfNewCopy = 0;
					}
					copyRetentionRules.add(indexOfNewCopy, newCopy);
					addItems();
				}
			};
		}

		table = new Table();
		table.setWidth("100%");
		table.setPageLength(0);

		table.setColumnHeader(CODE, $("FolderCopyRetentionRuleListTable.code"));
		table.setColumnHeader(COPY_TYPE, $("FolderCopyRetentionRuleListTable.copyType"));
		table.setColumnHeader(MEDIUM_TYPES, $("FolderCopyRetentionRuleListTable.mediumTypes"));
		table.setColumnHeader(CONTENT_TYPES_COMMENT, "");
		table.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.active"));
		table.setColumnHeader(ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.semiActive"));
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(INACTIVE_DISPOSAL_TYPE, $("FolderCopyRetentionRuleListTable.inactive"));
		table.setColumnHeader(INACTIVE_DISPOSAL_COMMENT, "");
		table.setColumnHeader(DELETE_BUTTON, "");

		if (formMode) {
			table.addContainerProperty(CODE, BaseTextField.class, null);
			table.addContainerProperty(COPY_TYPE, Label.class, null);
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
				FolderCopyRetentionRuleTable.this.retentionRuleVO.setCopyRetentionRules(newValue);
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

		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.active.code"));
		variablePeriodTable.setColumnHeader(ACTIVE_RETENTION_TITLE, $("FolderCopyRetentionRuleListTable.active.title"));

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
		List<CopyRetentionRule> copyRetentionRules = retentionRuleVO.getCopyRetentionRules();
		if (copyRetentionRules == null) {
			copyRetentionRules = new ArrayList<>();
			retentionRuleVO.setCopyRetentionRules(copyRetentionRules);
		}
		if (copyRetentionRules.isEmpty()) {
			CopyRetentionRule principalCopy = presenter.newFolderCopyRetentionRule(true);
			copyRetentionRules.add(principalCopy);

			CopyRetentionRule secondaryCopy = presenter.newFolderCopyRetentionRule(false);
			copyRetentionRules.add(secondaryCopy);
		}
		return copyRetentionRules;
	}

	@SuppressWarnings("unchecked")
	private void addItem(final CopyRetentionRule copyRetentionRule) {
		table.addItem(copyRetentionRule);

		if (formMode) {
			MiniTextField codeField = new MiniTextField();
			Label copyTypeLabel = new Label();
			MediumTypesField mediumTypesField = new MediumTypesField(copyRetentionRule);
			MiniTextField contentTypesCommentField = new MiniTextField();
			ActiveRetentionPeriodFieldGroup activeRetentionPeriodFieldGroup = new ActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			MiniTextField activeRetentionCommentField = new MiniTextField();
			SemiActiveRetentionPeriodFieldGroup semiActiveRetentionPeriodFieldGroup = new SemiActiveRetentionPeriodFieldGroup(
					copyRetentionRule);
			MiniTextField semiActiveRetentionCommentField = new MiniTextField();
			InactiveDisposalTypeField inactiveDisposalTypeField = new InactiveDisposalTypeField(copyRetentionRule);
			MiniTextField inactiveDisposalCommentField = new MiniTextField();

			copyTypeLabel.setConverter(copyTypeConverter);

			codeField.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
			copyTypeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, COPY_TYPE + ".code"));
			contentTypesCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CONTENT_TYPES_COMMENT));
			activeRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, ACTIVE_RETENTION_COMMENT));
			semiActiveRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT));
			inactiveDisposalCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT));

			table.getContainerProperty(copyRetentionRule, CODE).setValue(codeField);
			table.getContainerProperty(copyRetentionRule, COPY_TYPE).setValue(copyTypeLabel);
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

			if (copyRetentionRule.getCopyType() == CopyType.PRINCIPAL) {
				Button removeButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						getCopyRetentionRules().remove(copyRetentionRule);
						addItems();
					}
				};
				table.getContainerProperty(copyRetentionRule, DELETE_BUTTON).setValue(removeButton);
			} else {
				inactiveDisposalTypeField.setEnabled(false);
			}
		} else {
			Label codeLabel = new Label();
			Label copyTypeLabel = new Label();
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

			copyTypeLabel.setConverter(copyTypeConverter);
			mediumTypesLabel.setConverter(recordIdListToStringConverter);
			inactiveDisposalTypeLabel.setConverter(disposalTypeConverter);

			codeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
			copyTypeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, COPY_TYPE + ".code"));
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
		List<CopyRetentionRule> copyRetentionRules = getCopyRetentionRules();
		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			addItem(copyRetentionRule);
		}
	}

	@Override
	public List<CopyRetentionRule> getValue() {
		return retentionRuleVO.getCopyRetentionRules();
	}

	@Override
	public void setValue(List<CopyRetentionRule> newFieldValue)
			throws ReadOnlyException, ConversionException {
		retentionRuleVO.setCopyRetentionRules(newFieldValue);
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

			final RetentionPeriod retentionPeriod = (activeRetentionPeriod) ?
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
						.convertToPresentation(metadataCode, String.class, getLocale());
				Label metadataLabel = new Label(metadataLabelStr);
				Label retentionPeriodLabel = new Label("" + retentionPeriod.getValue());
				addComponents(metadataLabel, retentionPeriodLabel);
			}
		}

		public MetadataField getDateMetadataField() {
			return dateMetadataField;
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

			String documentTypeId = copyRetentionRule.getDocumentTypeId();
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
