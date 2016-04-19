package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category;

public interface ActionParametersCategoryField {
	
	String getFieldValue();
	
	void setFieldValue(String value);
	
	void addValueChangeListener(CategoryValueChangeListener listener);
	
	interface CategoryValueChangeListener {
		
		void valueChanged(String newValue);
		
	}

}
