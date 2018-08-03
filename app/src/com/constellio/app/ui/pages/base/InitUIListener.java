package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.application.ConstellioUI;

import java.io.Serializable;

public interface InitUIListener extends Serializable {

	void beforeInitialize(ConstellioUI ui);

	void afterInitialize(ConstellioUI ui);

}
