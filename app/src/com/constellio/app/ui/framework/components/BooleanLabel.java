package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Label;

import static com.constellio.app.ui.i18n.i18n.$;

public class BooleanLabel extends Label {
	public BooleanLabel(boolean value) {
		super($(value ? "yes" : "no"));
	}
}
