package com.constellio.app.ui.pages.batchprocess;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;

public class ListBatchProcessesPresenter extends BasePresenter<ListBatchProcessesView> {

	public ListBatchProcessesPresenter(ListBatchProcessesView view) {
		super(view);
	}

	public ListBatchProcessesPresenter(ListBatchProcessesView view, ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, constellioFactories, sessionContext);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}
	
	private boolean areSystemBatchProcessesVisible(User user) {
		return true;
	}

}
