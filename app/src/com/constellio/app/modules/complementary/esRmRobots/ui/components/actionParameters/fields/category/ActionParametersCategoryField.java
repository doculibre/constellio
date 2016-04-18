package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category;

public interface ActionParametersCategoryField {
	
	String getValue();
	
	void setValue(String value);
	
	void addValueChangeListener(CategoryValueChangeListener listener);
	
	interface CategoryValueChangeListener {
		
		void valueChanged(String oldValue, String newValue);
		
	}

}
