package com.constellio.app.modules.rm.ui.components.tree;

import com.constellio.app.ui.pages.base.SessionContextProvider;

public interface RMTreeDropHander extends SessionContextProvider {

	void showErrorMessage(String errorMessage);

}
