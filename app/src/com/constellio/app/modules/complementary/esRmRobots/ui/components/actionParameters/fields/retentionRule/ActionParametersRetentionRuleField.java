package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule;

import java.util.List;

public interface ActionParametersRetentionRuleField {

	void setOptions(List<String> options);
	
	String getFieldValue();
	
	void setFieldValue(String value);

}
