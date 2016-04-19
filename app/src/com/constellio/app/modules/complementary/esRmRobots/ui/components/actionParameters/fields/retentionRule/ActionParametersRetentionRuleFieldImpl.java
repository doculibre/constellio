package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;

public class ActionParametersRetentionRuleFieldImpl extends RecordComboBox implements ActionParametersRetentionRuleField {
	
	private RecordIdToCaptionConverter converter = new RecordIdToCaptionConverter();

	public ActionParametersRetentionRuleFieldImpl() {
		super(RetentionRule.DEFAULT_SCHEMA);
	}

	@Override
	public void setOptions(List<String> options) {
		removeAllItems();
		for (String option : options) {
			addItem(option);
			setItemCaption(option, converter.convertToPresentation(option, String.class, getLocale()));
		}
	}

	@Override
	public String getFieldValue() {
		return (String) getInternalValue();
	}

	@Override
	public void setFieldValue(String value) {
		setInternalValue(value);
	}

}
