package com.constellio.app.modules.rm.services.menu.behaviors.ui;

import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;

import java.util.List;

public class CartBatchProcessingViewImpl implements BatchProcessingView {
	private final String schemaType;
	private CartBatchProcessingPresenter cartBatchProcessingPresenter;

	public CartBatchProcessingViewImpl(String schemaType,
									   CartBatchProcessingPresenter cartBatchProcessingPresenter) {
		this.schemaType = schemaType;
		this.cartBatchProcessingPresenter = cartBatchProcessingPresenter;
	}

	@Override
	public List<String> getSelectedRecordIds() {
		return cartBatchProcessingPresenter.getNotDeletedRecordsIds(schemaType);
	}

	@Override
	public List<String> getUnselectedRecordIds() {
		return null;
	}

	@Override
	public String getSchemaType() {
		return schemaType;
	}

	@Override
	public SessionContext getSessionContext() {
		return cartBatchProcessingPresenter.getView().getSessionContext();
	}

	@Override
	public void showErrorMessage(String error) {
		cartBatchProcessingPresenter.getView().showErrorMessage(error);
	}

	@Override
	public void showMessage(String message) {
		cartBatchProcessingPresenter.getView().showMessage(message);
	}
}