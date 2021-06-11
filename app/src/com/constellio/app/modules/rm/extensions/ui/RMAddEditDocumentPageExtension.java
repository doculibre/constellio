package com.constellio.app.modules.rm.extensions.ui;

import java.util.Locale;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RMAddEditDocumentPageExtension {
	
	protected AppLayerFactory appLayerFactory;
	
	public RMAddEditDocumentPageExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void afterSaveDocument(AfterSaveDocumentParams params) {
	}
	
	@Getter
	@AllArgsConstructor
	public static class AfterSaveDocumentParams {
		Document document;
		User user;
		Map<String, String> paramsMap;
		Locale locale;
	}

}
