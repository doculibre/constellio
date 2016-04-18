package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule;

import java.util.ArrayList;

import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface ActionParametersRetentionRuleField extends SessionContextProvider {

	void setOptions(ArrayList<String> options);
	
	String getValue();
	
	void setValue(String value);
	
	void addValueChangeListener(RetentionRuleValueChangeListener listener);
	
	interface RetentionRuleValueChangeListener {
		
		void valueChanged(String oldValue, String newValue);
		
	}

}
