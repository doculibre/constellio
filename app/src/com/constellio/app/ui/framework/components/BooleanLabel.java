package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.ui.Label;

public class BooleanLabel extends Label {
	public BooleanLabel(boolean value) {
		super($(value ? "yes" : "no"));
	}
}
