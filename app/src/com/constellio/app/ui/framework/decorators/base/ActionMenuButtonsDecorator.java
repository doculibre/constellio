package com.constellio.app.ui.framework.decorators.base;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.ui.Button;

public interface ActionMenuButtonsDecorator extends Serializable {
	
	void decorate(BaseViewImpl view, List<Button> actionMenuButtons);

}
