package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.UIContext;

import java.util.Map;

public class DocumentFolderBreadcrumParams {
	String id;
	Map<String,String> documentParams;
	BaseView baseView;
	UIContext uiContext;

	public DocumentFolderBreadcrumParams(String id, Map<String,String> documentParams, BaseView baseView) {
		this.id = id;
		this.documentParams = documentParams;
		this.baseView = baseView;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getDocumentParams() {
		return documentParams;
	}

	public BaseView getBaseView() {
		return baseView;
	}
}
