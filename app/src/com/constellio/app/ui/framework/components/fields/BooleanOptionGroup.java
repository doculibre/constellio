package com.constellio.app.ui.framework.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.ui.OptionGroup;

public class BooleanOptionGroup extends OptionGroup {

	public BooleanOptionGroup() {
		setNullSelectionAllowed(false);
		addItem(true);
		addItem(false);
		setItemCaption(true, $("true"));
		setItemCaption(false, $("false"));
	}

}
