package com.constellio.app.ui.pages.management.extractors.fields;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.regex.Pattern;

import static com.constellio.app.ui.i18n.i18n.$;

public class RegexConfigFieldImpl extends CustomField<RegexConfigVO> implements RegexConfigField {

	private RegexConfigVO regexConfigVO;

	private BeanItem<RegexConfigVO> regexConfigItem;

	private FieldGroup fieldGroup;

	@PropertyId("inputMetadata")
	private ComboBox inputMetadataComboboxField;

	@PropertyId("regex")
	private TextField regexTextField;

	@PropertyId("value")
	private TextField valueTextField;

	@PropertyId("regexConfigType")
	private ComboBox regexConfigTypeComboboxField;

	@Override
	protected Component initContent() {
		if (regexConfigVO == null) {
			regexConfigVO = new RegexConfigVO();
		}
		regexConfigItem = new BeanItem<>(regexConfigVO);
		fieldGroup = new FieldGroup(regexConfigItem);

		setPropertyDataSource(new AbstractProperty<RegexConfigVO>() {
			@Override
			public RegexConfigVO getValue() {
				boolean submittedMetadataValid = regexConfigVO.getInputMetadata() != null;
				return submittedMetadataValid ? regexConfigVO : null;
			}

			@Override
			public void setValue(RegexConfigVO newValue)
					throws ReadOnlyException {
				setInternalValue(newValue);
				regexConfigVO = newValue != null ? newValue : new RegexConfigVO();
				if (fieldGroup != null) {
					regexConfigItem = new BeanItem<>(regexConfigVO);
					fieldGroup.setItemDataSource(regexConfigItem);
				}
			}

			@Override
			public Class<? extends RegexConfigVO> getType() {
				return RegexConfigVO.class;
			}
		});

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("99%");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);

		inputMetadataComboboxField = new ComboBox();
		inputMetadataComboboxField.setCaption($("RegexConfigField.inputMetadata"));
		inputMetadataComboboxField.setId("inputMetadata");
		inputMetadataComboboxField.addItem("test");
		inputMetadataComboboxField.setNullSelectionAllowed(false);

		regexTextField = new BaseTextField();
		regexTextField.setCaption($("RegexConfigField.regex"));
		regexTextField.setId("regex");
		regexTextField.setNullRepresentation("");

		valueTextField = new BaseTextField();
		valueTextField.setCaption($("RegexConfigField.value"));
		valueTextField.setId("value");
		valueTextField.setNullRepresentation("");

		regexConfigTypeComboboxField = new ComboBox();
		regexConfigTypeComboboxField.setCaption($("RegexConfigField.regexConfigType"));
		regexConfigTypeComboboxField.setId("regexConfigType");
		regexConfigTypeComboboxField.addItem(RegexConfigType.SUBSTITUTION);
		regexConfigTypeComboboxField
				.setItemCaption(RegexConfigType.SUBSTITUTION, $("RegexConfigField.RegexConfigType.SUBSTITUTION"));
		regexConfigTypeComboboxField.addItem(RegexConfigType.TRANSFORMATION);
		regexConfigTypeComboboxField
				.setItemCaption(RegexConfigType.TRANSFORMATION, $("RegexConfigField.RegexConfigType.TRANSFORMATION"));

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setWidth("100%");
		horizontalLayout.addComponents(inputMetadataComboboxField, regexTextField, regexConfigTypeComboboxField, valueTextField);

		mainLayout.addComponents(horizontalLayout);
		mainLayout.setExpandRatio(horizontalLayout, 1);

		fieldGroup.bindMemberFields(this);

		return mainLayout;
	}

	public void setMetadataOptions(List<MetadataVO> metadataVOs) {
		inputMetadataComboboxField.removeAllItems();
		for (MetadataVO metadataVO : metadataVOs) {
			inputMetadataComboboxField.addItem(metadataVO.getCode());
			inputMetadataComboboxField.setItemCaption(metadataVO.getCode(), metadataVO.getLabel());
		}
	}

	@Override
	public Class<? extends RegexConfigVO> getType() {
		return RegexConfigVO.class;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		if (isValidField()) {
			try {
				fieldGroup.commit();
			} catch (CommitException e) {
				throw new InvalidValueException(e.getMessage());
			}
			super.commit();
		}
	}

	private boolean isValidField() {
		boolean valid = true;
		if (inputMetadataComboboxField.getValue() == null
			|| regexTextField.getValue() == null
			|| valueTextField.getValue() == null
			|| regexConfigTypeComboboxField.getValue() == null) {
			valid = false;
		} else {
			try {
				Pattern.compile(regexTextField.getValue());
			} catch (Exception e) {
				valid = false;
			}
		}
		return valid;
	}
}
