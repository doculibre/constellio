package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.framework.components.BaseWindow;

public class BaseWindowParams {
	
	private BaseWindow window;

	public BaseWindowParams(BaseWindow window) {
		this.window = window;
	}

	public BaseWindow getWindow() {
		return window;
	}

}
