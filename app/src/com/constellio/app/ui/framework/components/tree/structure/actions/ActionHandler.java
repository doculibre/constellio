package com.constellio.app.ui.framework.components.tree.structure.actions;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;

public class ActionHandler implements Handler {

	private final Action actionRegistered;
	private final OnActionDelegate actionDelegate;

	public ActionHandler(Action actionRegistered,
						 OnActionDelegate actionDelegate) {
		this.actionRegistered = actionRegistered;
		this.actionDelegate = actionDelegate;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[]{actionRegistered};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionRegistered && actionDelegate != null) {
			actionDelegate.execute(sender, target);
		}
	}

	public interface OnActionDelegate {
		void execute(Object sender, Object target);
	}
}
