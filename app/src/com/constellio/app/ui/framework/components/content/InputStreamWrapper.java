package com.constellio.app.ui.framework.components.content;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InputStreamWrapper {
	private InputStream inputStream;
	private boolean actionDone = false;
	private List<SimpleAction> simpleActionList = new ArrayList<>();

	public interface SimpleAction {
		void action(InputStreamWrapper inputStreamWrapper);
	}

	public InputStreamWrapper() {

	}

	public void doActionIfNotAlreadyDone() {
		if(!actionDone) {
			for(SimpleAction simpleAction : simpleActionList) {
				simpleAction.action(this);
			}
			actionDone = true;
		}
	}

	public void addSimpleAction(SimpleAction simpleAction) {
		simpleActionList.add(simpleAction);
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public boolean isActionDone() {
		return actionDone;
	}

	public void setActionDone(boolean actionDone) {
		this.actionDone = actionDone;
	}
}
