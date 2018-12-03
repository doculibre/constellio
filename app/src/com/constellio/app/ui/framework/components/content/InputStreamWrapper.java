package com.constellio.app.ui.framework.components.content;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InputStreamWrapper {
	private InputStream inputStream;
	private List<SimpleAction> simpleActionList = new ArrayList<>();

	public interface SimpleAction {
		void action(InputStreamWrapper inputStreamWrapper);
	}

	public InputStreamWrapper() {

	}

	public void doActionIfNotAlreadyDone() {
		for(SimpleAction simpleAction : simpleActionList) {
			simpleAction.action(this);
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
}
