package com.constellio.app.ui.framework.components.tree.structure.actions;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;

public class EnterPressActionHandler extends ActionHandler {

	public EnterPressActionHandler(OnActionDelegate actionDelegate) {
		super(new ShortcutAction("", KeyCode.ENTER, null), actionDelegate);
	}
}
