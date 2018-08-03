package com.constellio.app.ui.framework.decorators.base;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.ui.Button;

import java.io.Serializable;
import java.util.List;

public interface ActionMenuButtonsDecorator extends Serializable {

	void decorate(BaseViewImpl view, List<Button> actionMenuButtons);

}
