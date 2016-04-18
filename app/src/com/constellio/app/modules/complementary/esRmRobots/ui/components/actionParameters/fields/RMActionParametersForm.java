package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields;

import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleField;
import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface RMActionParametersForm extends SessionContextProvider {
	
	ActionParametersCategoryField getCategoryField();
	
	ActionParametersRetentionRuleField getRetentionRuleField();

}
