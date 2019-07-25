package com.constellio.app.services.menu.behavior.ui;

import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;

import java.util.List;

public class AdvancedViewBatchProcessingViewImpl implements BatchProcessingView {

	private AdvancedViewBatchProcessingPresenter batchProcessingPresenter;

	public AdvancedViewBatchProcessingViewImpl(AdvancedViewBatchProcessingPresenter batchProcessingPresenter) {
		this.batchProcessingPresenter = batchProcessingPresenter;
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return ((AdvancedSearchView) batchProcessingPresenter.getView()).getSelectedRecordIds();
	}

	@Override
	public List<String> getUnselectedRecordIds() {
		return ((AdvancedSearchView) batchProcessingPresenter.getView()).getUnselectedRecordIds();
	}

	@Override
	public String getSchemaType() {
		return ((AdvancedSearchView) batchProcessingPresenter.getView()).getSchemaType();
	}

	@Override
	public SessionContext getSessionContext() {
		return batchProcessingPresenter.getView().getSessionContext();
	}

	@Override
	public void showErrorMessage(String error) {
		batchProcessingPresenter.getView().showErrorMessage(error);
	}

	@Override
	public void showMessage(String message) {
		batchProcessingPresenter.getView().showMessage(message);
	}
}
