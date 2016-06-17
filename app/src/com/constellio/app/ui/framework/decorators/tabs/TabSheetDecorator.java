package com.constellio.app.ui.framework.decorators.tabs;

import java.io.Serializable;

import com.vaadin.navigator.View;
import com.vaadin.ui.TabSheet;

public interface TabSheetDecorator extends Serializable {
	
	void decorate(View view, TabSheet tabSheet);

}
