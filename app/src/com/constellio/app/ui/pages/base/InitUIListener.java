package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioUI;

public interface InitUIListener extends Serializable {
	
	void beforeInitialize(ConstellioUI ui);
	
	void afterInitialize(ConstellioUI ui);
	
}
