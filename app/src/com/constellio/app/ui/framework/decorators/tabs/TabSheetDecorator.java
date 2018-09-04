package com.constellio.app.ui.framework.decorators.tabs;

import com.vaadin.navigator.View;
import com.vaadin.ui.TabSheet;

import java.io.Serializable;

public interface TabSheetDecorator extends Serializable {

	void decorate(View view, TabSheet tabSheet);

}
