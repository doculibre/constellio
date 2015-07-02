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
package com.constellio.app.modules.rm.ui.components.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionType;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdListToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class CopyRetentionRuleTable extends CustomField<List<CopyRetentionRule>> {
	private static final String CODE = "code";
	private static final String COPY_TYPE = "copyType";
	private static final String MEDIUM_TYPES = "mediumTypeIds";
	private static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	private static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
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
	private RetentionRuleVO retentionRuleVO;
	private VerticalLayout mainLayout;
	private AddButton addButton;
	private Table table;
	private boolean formMode;

	public CopyRetentionRuleTable(RetentionRuleVO retentionRuleVO, boolean formMode,
			List<VariableRetentionPeriodVO> variableRetentionPeriodVOList) {
		this.retentionRuleVO = retentionRuleVO;
		this.formMode = formMode;
		this.variableRetentionPeriodVOList = variableRetentionPeriodVOList;

		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		if (formMode) {
			addButton = new AddButton($("CopyRetentionRuleListTable.addPrincipalCopy")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					CopyRetentionRule newCopy = newCopy(true);
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

		table.setColumnHeader(CODE, $("CopyRetentionRuleListTable.code"));
		table.setColumnHeader(COPY_TYPE, $("CopyRetentionRuleListTable.copyType"));
		table.setColumnHeader(MEDIUM_TYPES, $("CopyRetentionRuleListTable.mediumTypes"));
		table.setColumnHeader(CONTENT_TYPES_COMMENT, "");
		table.setColumnHeader(ACTIVE_RETENTION_PERIOD, $("CopyRetentionRuleListTable.active"));
		table.setColumnHeader(ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_PERIOD, $("CopyRetentionRuleListTable.semiActive"));
		table.setColumnHeader(SEMI_ACTIVE_RETENTION_COMMENT, "");
		table.setColumnHeader(INACTIVE_DISPOSAL_TYPE, $("CopyRetentionRuleListTable.inactive"));
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
				CopyRetentionRuleTable.this.retentionRuleVO.setCopyRetentionRules(newValue);
			}
		});

		if (addButton != null) {
			mainLayout.addComponent(addButton);
			mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		}
		mainLayout.addComponent(table);

		addItems();
	}

	protected void onDisposalTypeChange(CopyRetentionRule rule) {
	}

	private CopyRetentionRule newCopy(boolean principal) {
		CopyRetentionRule newCopy = new CopyRetentionRule();
		if (principal) {
			newCopy.setCopyType(CopyType.PRINCIPAL);
		} else {
			newCopy.setCopyType(CopyType.SECONDARY);
			newCopy.setInactiveDisposalType(DisposalType.DESTRUCTION);
		}
		newCopy.setActiveRetentionPeriod(RetentionPeriod.ZERO);
		newCopy.setSemiActiveRetentionPeriod(RetentionPeriod.ZERO);
		return newCopy;
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
			CopyRetentionRule principalCopy = newCopy(true);
			copyRetentionRules.add(principalCopy);

			CopyRetentionRule secondaryCopy = newCopy(false);
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

		public RetentionPeriodFieldGroup(final CopyRetentionRule copyRetentionRule, final boolean activeRetentionPeriod) {
			setSpacing(true);

			RetentionPeriod retentionPeriod = (activeRetentionPeriod) ?
					copyRetentionRule.getActiveRetentionPeriod() :
					copyRetentionRule.getSemiActiveRetentionPeriod();

			if (formMode) {
				BeanItemContainer<VariableRetentionPeriodVO> container =
						new BeanItemContainer<>(VariableRetentionPeriodVO.class, getVariablePeriods());
				final ComboBox openRetentionPeriodDDVField = new ComboBox("", container);
				openRetentionPeriodDDVField.setInputPrompt($("fixedPeriod"));
				openRetentionPeriodDDVField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
				openRetentionPeriodDDVField.setItemCaptionPropertyId("code");

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

				addComponents(openRetentionPeriodDDVField, yearsField);
			} else {
				Label retentionPeriodLabel = new Label("" + retentionPeriod.getValue());
				addComponent(retentionPeriodLabel);
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

			addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					onDisposalTypeChange(copyRetentionRule);
				}
			});
		}

	}

}
