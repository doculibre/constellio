package com.constellio.app.ui.framework.components.tree.structure.actions;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;

public class EscapePressActionHandler extends ActionHandler {

	public EscapePressActionHandler(OnActionDelegate actionDelegate) {
		super(new ShortcutAction("", KeyCode.ESCAPE, null), actionDelegate);
	}
}
