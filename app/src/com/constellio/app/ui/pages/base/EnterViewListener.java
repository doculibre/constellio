package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.vaadin.navigator.View;

public interface EnterViewListener extends Serializable {
	
	void enterView(View view);

}
