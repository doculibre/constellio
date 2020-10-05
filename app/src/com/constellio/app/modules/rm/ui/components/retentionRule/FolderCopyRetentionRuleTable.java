package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.ui.components.converters.MediumTypeIdListToCodesConverter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.BooleanLabel;
import com.constellio.app.ui.framework.components.converters.BaseStringToIntegerConverter;
import com.constellio.app.ui.framework.components.converters.MetadataCodeToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderCopyRetentionRuleTable extends CustomField<List<CopyRetentionRule>> {

	private static final String CODE = "code";
	private static final String DETAILS = "details";
	private static final String COPY_TYPE = "copyType";
	private static final String FOLDER_TYPE = "typeId";
	private static final String MEDIUM_TYPES = "mediumTypeIds";
	private static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	private static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
	private static final String ACTIVE_RETENTION_TITLE = "activeRetentionTitle";
	private static final String ACTIVE_RETENTION_COMMENT = "activeRetentionComment";
	private static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
	private static final String SEMI_ACTIVE_RETENTION_COMMENT = "semiActiveRetentionComment";
	private static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	private static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
	private static final String ESSENTIAL = "essential";
	private static final String DELETE_BUTTON = "deleteButton";

	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String OPEN_ACTIVE_RETENTION_PERIOD = "openActiveRetentionPeriod";

	private final List<VariableRetentionPeriodVO> variableRetentionPeriodVOList;

	private RecordIdToCaptionConverter folderTypeConverter = new RecordIdToCaptionConverter();
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
	private Locale locale;

	public FolderCopyRetentionRuleTable(RetentionRuleVO retentionRuleVO, boolean formMode,
										final RetentionRuleTablePresenter presenter, Locale locale) {
		this.retentionRuleVO = retentionRuleVO;
		this.formMode = formMode;
		this.presenter = presenter;
		this.variableRetentionPeriodVOList = presenter.getOpenPeriodsDDVList();
		this.locale = locale;

		addStyleName("copy-retention-rule-table");
		addStyleName("folder-copy-retention-rule-table");

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

		table = new BaseTable(getClass().getName());
		table.addStyleName("copy-retention-rule-table");
		table.setWidth("100%");
		table.setPageLength(0);
		table.setSelectable(false);

		table.setColumnHeader(CODE, $("FolderCopyRetentionRuleListTable.code"));
		table.setColumnHeader(TITLE, $("FolderCopyRetentionRuleListTable.title"));
		table.setColumnHeader(DETAILS, $("FolderCopyRetentionRuleListTable.details"));
		table.setColumnHeader(OPEN_ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.openActiveRetentionPeriod"));
		table.setColumnHeader(COPY_TYPE, $("FolderCopyRetentionRuleListTable.copyType"));
		table.setColumnHeader(MEDIUM_TYPES, $("FolderCopyRetentionRuleListTable.mediumTypes"));
		table.setColumnHeader(CONTENT_TYPES_COMMENT, "");
		table.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.active"));
		table.setColumnHeader(ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_PERIOD, $("FolderCopyRetentionRuleListTable.semiActive"));
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(INACTIVE_DISPOSAL_TYPE, $("FolderCopyRetentionRuleListTable.inactive"));
		table.setColumnHeader(INACTIVE_DISPOSAL_COMMENT, "");
		table.setColumnHeader(ESSENTIAL, $("FolderCopyRetentionRuleListTable.essential"));
		table.setColumnHeader(DELETE_BUTTON, "");

		if (formMode) {
			table.setColumnWidth(CODE, 50);
			table.setColumnWidth(DETAILS, 70);
			table.setColumnWidth(COPY_TYPE, 225);
			table.setColumnWidth(MEDIUM_TYPES, 200);
			table.setColumnWidth(CONTENT_TYPES_COMMENT, 50);
			table.setColumnWidth(ACTIVE_RETENTION_PERIOD, 158);
			table.setColumnWidth(ACTIVE_RETENTION_COMMENT, 50);
			table.setColumnWidth(SEMI_ACTIVE_RETENTION_PERIOD, 158);
			table.setColumnWidth(SEMI_ACTIVE_RETENTION_COMMENT, 50);
			table.setColumnWidth(INACTIVE_DISPOSAL_TYPE, 158);
			table.setColumnWidth(INACTIVE_DISPOSAL_COMMENT, 50);
			table.setColumnWidth(ESSENTIAL, 50);
//			table.setColumnWidth(TITLE, 200);
			table.setColumnWidth(DELETE_BUTTON, 50);

			table.addContainerProperty(CODE, BaseTextField.class, null);
			table.addContainerProperty(TITLE, BaseTextField.class, null);
			table.addContainerProperty(DETAILS, DetailsFieldGroup.class, null);
			table.addContainerProperty(COPY_TYPE, CopyTypeFolderTypePanel.class, null);
			table.addContainerProperty(MEDIUM_TYPES, MediumTypesField.class, null);
			table.addContainerProperty(CONTENT_TYPES_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_PERIOD, ActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_PERIOD, SemiActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_TYPE, InactiveDisposalTypeField.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_COMMENT, MiniTextField.class, null);
			table.addContainerProperty(ESSENTIAL, CheckBox.class, null);
			table.addContainerProperty(DELETE_BUTTON, Button.class, null);
		} else {
			table.setColumnWidth(CODE, 50);
			table.setColumnWidth(COPY_TYPE, 100);
			table.setColumnWidth(MEDIUM_TYPES, 100);
			table.setColumnWidth(CONTENT_TYPES_COMMENT, 50);
			table.setColumnWidth(ACTIVE_RETENTION_PERIOD, 120);
			table.setColumnWidth(ACTIVE_RETENTION_COMMENT, 50);
			table.setColumnWidth(SEMI_ACTIVE_RETENTION_PERIOD, 120);
			table.setColumnWidth(SEMI_ACTIVE_RETENTION_COMMENT, 50);
			table.setColumnWidth(INACTIVE_DISPOSAL_TYPE, 50);
			table.setColumnWidth(INACTIVE_DISPOSAL_COMMENT, 50);
			table.setColumnWidth(ESSENTIAL, 50);
			table.setColumnWidth(TITLE, 200);
			table.setColumnExpandRatio(TITLE, 1);

			table.addContainerProperty(CODE, Label.class, null);
			table.addContainerProperty(TITLE, Label.class, null);
			table.addContainerProperty(COPY_TYPE, CopyTypeFolderTypePanel.class, null);
			table.addContainerProperty(MEDIUM_TYPES, Label.class, null);
			table.addContainerProperty(CONTENT_TYPES_COMMENT, Label.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_PERIOD, ActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(ACTIVE_RETENTION_COMMENT, Label.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_PERIOD, SemiActiveRetentionPeriodFieldGroup.class, null);
			table.addContainerProperty(SEMI_ACTIVE_RETENTION_COMMENT, Label.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_TYPE, Label.class, null);
			table.addContainerProperty(INACTIVE_DISPOSAL_COMMENT, Label.class, null);
			table.addContainerProperty(ESSENTIAL, Label.class, null);
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
		variablePeriodTable = new BaseTable(getClass().getName() + ".variablePeriodTable");
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
			BaseTextField titleField = new BaseTextField();
			DetailsFieldGroup detailsField = new DetailsFieldGroup(copyRetentionRule);
			CopyTypeFolderTypePanel copyTypeLabel = new CopyTypeFolderTypePanel(copyRetentionRule);
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
			CheckBox essential = new CheckBox();

			codeField.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
			titleField.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, TITLE));
			contentTypesCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CONTENT_TYPES_COMMENT));
			activeRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, ACTIVE_RETENTION_COMMENT));
			semiActiveRetentionCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, SEMI_ACTIVE_RETENTION_COMMENT));
			inactiveDisposalCommentField
					.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, INACTIVE_DISPOSAL_COMMENT));
			essential.setPropertyDataSource(new NestedMethodProperty<Boolean>(copyRetentionRule, ESSENTIAL));

			table.getContainerProperty(copyRetentionRule, CODE).setValue(codeField);
			table.getContainerProperty(copyRetentionRule, TITLE).setValue(titleField);
			table.getContainerProperty(copyRetentionRule, DETAILS).setValue(detailsField);
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
			table.getContainerProperty(copyRetentionRule, ESSENTIAL).setValue(essential);

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
			BaseLabel titleLabel = new BaseLabel(copyRetentionRule.getTitle());
			CopyTypeFolderTypePanel copyTypeLabel = new CopyTypeFolderTypePanel(copyRetentionRule);
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
			Label essential = new BooleanLabel(copyRetentionRule.isEssential());

			StringBuilder niceTitle = new StringBuilder();
			if (StringUtils.isNotBlank(copyRetentionRule.getDescription())) {
				niceTitle.append(copyRetentionRule.getDescription());
				niceTitle.append("<br/><br/>");
			}
			if (copyRetentionRule.isIgnoreActivePeriod()) {
				niceTitle.append($("DetailsFieldGroup.ignoreActivePeriod"));
				niceTitle.append(":");
				niceTitle.append($("" + copyRetentionRule.isIgnoreActivePeriod()));
			}

			titleLabel.addStyleName(ValoTheme.BUTTON_LINK);
			titleLabel.addExtension(new NiceTitle(niceTitle.toString()));
			mediumTypesLabel.setConverter(mediumTypeIdListToCodesConverter);
			//			inactiveDisposalTypeLabel.setConverter(disposalTypeConverter);

			codeLabel.setPropertyDataSource(new NestedMethodProperty<String>(copyRetentionRule, CODE));
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
			table.getContainerProperty(copyRetentionRule, TITLE).setValue(titleLabel);
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
			table.getContainerProperty(copyRetentionRule, ESSENTIAL).setValue(essential);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
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

		@Override
		protected RecordComboBox newAddEditField() {
			RecordComboBox field = super.newAddEditField();
			field.setWidth("80px");
			return field;
		}
	}

	private class CopyTypeFolderTypePanel extends VerticalLayout {
		public CopyTypeFolderTypePanel(CopyRetentionRule rule) {
			CopyType copyType = rule.getCopyType() == null ? CopyType.PRINCIPAL : rule.getCopyType();
			Label copyTypeLabel = new Label($("CopyType." + copyType.getCode()));
			addComponent(copyTypeLabel);

			if (formMode) {
				LookupRecordField folderType = new LookupRecordField(FolderType.SCHEMA_TYPE);
				folderType.setPropertyDataSource(new NestedMethodProperty<String>(rule, FOLDER_TYPE));
				addComponent(folderType);
			} else if (rule.getTypeId() != null) {
				Label folderType = new Label();
				folderType.setConverter(folderTypeConverter);
				folderType.setPropertyDataSource(new NestedMethodProperty<String>(rule, FOLDER_TYPE));
				addComponent(folderType);
			}

			setSpacing(true);
		}
	}

	private class RetentionPeriodFieldGroup extends VerticalLayout {

		private MetadataField dateMetadataField;

		public RetentionPeriodFieldGroup(final CopyRetentionRule copyRetentionRule,
										 final boolean activeRetentionPeriod) {
			setSpacing(true);
			setWidth("150px");

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
				final ComboBox openRetentionPeriodDDVField = new BaseComboBox("", container);
				openRetentionPeriodDDVField.setInputPrompt($("fixedPeriod"));
				openRetentionPeriodDDVField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
				for (VariableRetentionPeriodVO periodVO : container.getItemIds()) {
					String title;
					if (periodVO.getCode().equals("888") || periodVO.getCode().equals("999")) {
						title = $("variablePeriod" + periodVO.getCode());
					} else {
						title = periodVO.getTitle();
					}
					openRetentionPeriodDDVField.setItemCaption(periodVO, periodVO.getCode() + " - " + title);
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
							break;
						}
					}
					if (activeRetentionPeriod &&
						(retentionPeriod.getRetentionType() == RetentionType.OPEN ||
						 retentionPeriod.getRetentionType() == RetentionType.UNTIL_REPLACED)) {
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
												  (newRetentionPeriod.getRetentionType() == RetentionType.OPEN ||
												   newRetentionPeriod.getRetentionType() == RetentionType.UNTIL_REPLACED));
						} else {
							copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(0));

							if(yearsField.isEnabled()) {
								yearsField.setValue("0");
							}

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
							if (activeRetentionPeriod &&
								(period.getRetentionType() == RetentionType.OPEN ||
								 period.getRetentionType() == RetentionType.UNTIL_REPLACED)) {
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

	private class DetailsFieldGroup extends VerticalLayout {

		private BaseTextArea descriptionField;
		private CheckBox ignoreActivePeriodField;
		private ComboBox semiActiveYearTypeField;
		private ComboBox inactiveYearTypeField;

		public DetailsFieldGroup(final CopyRetentionRule copyRetentionRule) {
			WindowConfiguration windowConfiguration = WindowConfiguration.modalDialog("50%", "500px");
			final WindowButton windowButton = new WindowButton($("DetailsFieldGroup.detailsButton"),
					$("DetailsFieldGroup.detailsWindow"), windowConfiguration) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout windowLayout = new VerticalLayout();

					windowLayout.setSpacing(true);

					Property<String> descriptionProperty = new MethodProperty<>(copyRetentionRule, "description");
					Property<Boolean> ignoreActivePeriodProperty = new MethodProperty<>(copyRetentionRule, "ignoreActivePeriod");
					Property<Boolean> semiActiveYearTypeProperty = new MethodProperty<>(copyRetentionRule,
							"semiActiveYearTypeId");
					Property<Boolean> inactiveYearTypeProperty = new MethodProperty<>(copyRetentionRule, "inactiveYearTypeId");

					descriptionField = new BaseTextArea($("DetailsFieldGroup.description"), descriptionProperty);
					ignoreActivePeriodField = new CheckBox($("DetailsFieldGroup.ignoreActivePeriod"), ignoreActivePeriodProperty);

					semiActiveYearTypeField = new RecordComboBox(YearType.DEFAULT_SCHEMA);
					semiActiveYearTypeField.setCaption($("DetailsFieldGroup.semiActiveYearType"));
					semiActiveYearTypeField.setPropertyDataSource(semiActiveYearTypeProperty);
					semiActiveYearTypeField.setVisible(Toggle.RETENTION_RULE_DATE_TYPES.isEnabled());

					inactiveYearTypeField = new RecordComboBox(YearType.DEFAULT_SCHEMA);
					inactiveYearTypeField.setCaption($("DetailsFieldGroup.inactiveYearType"));
					inactiveYearTypeField.setPropertyDataSource(inactiveYearTypeProperty);
					inactiveYearTypeField.setVisible(Toggle.RETENTION_RULE_DATE_TYPES.isEnabled());

					if (DetailsFieldGroup.this.isReadOnly()) {
						descriptionField.setReadOnly(true);
						ignoreActivePeriodProperty.setReadOnly(true);
					}

					descriptionField.setWidth("90%");

					Button closeButton = new BaseButton($("Ok")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};
					closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

					windowLayout.addComponents(descriptionField, ignoreActivePeriodField, semiActiveYearTypeField,
							inactiveYearTypeField, closeButton);
					return windowLayout;
				}
			};
			addComponent(windowButton);

			StringBuilder niceTitleContent = new StringBuilder();
			if (StringUtils.isNotBlank(copyRetentionRule.getTitle())) {
				niceTitleContent.append(copyRetentionRule.getTitle());
			}
			if (StringUtils.isNotBlank(copyRetentionRule.getDescription())) {
				niceTitleContent.append("<br/>");
				niceTitleContent.append("<br/>");
				niceTitleContent.append(copyRetentionRule.getDescription());
			}
			if (niceTitleContent.length() > 0) {
				addExtension(new NiceTitle(niceTitleContent.toString()));
			}
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
			setWidth("150px");

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

			for (MetadataVO metadataVO : dateMetadataVOs) {
				String metatadaCode = metadataVO.getCode();
				addItem(metatadaCode);
				setItemCaption(metatadaCode, metadataVO.getLabel(locale));
			}
		}

	}

}
