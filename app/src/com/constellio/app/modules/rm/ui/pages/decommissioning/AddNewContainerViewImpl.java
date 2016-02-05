package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerViewImpl;

public class AddNewContainerViewImpl extends AddEditContainerViewImpl implements AddNewContainerView {
	public AddNewContainerViewImpl() {
		presenter = new AddNewContainerPresenter(this);
	}
}
