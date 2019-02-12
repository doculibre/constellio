package com.constellio.app.ui.pages.SIP;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.records.wrappers.User;

public class SIPProgressionViewPresenter extends BasePresenter<SIPProgressionViewImpl> {

	private ProgressInfo progressInfo;

	public SIPProgressionViewPresenter(SIPProgressionViewImpl view, String id) {
		super(view);
		setTask(id);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	protected AsyncTaskBatchProcess getTask(String id) {
		return (AsyncTaskBatchProcess) modelLayerFactory.getBatchProcessesManager().get(id);
	}

	public ProgressInfo getProgressInfo() {
		return progressInfo;
	}

	protected void setTask(String id) {
		progressInfo = new ProgressInfo();
	}
}
