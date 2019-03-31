package com.constellio.app.modules.rm.ui.components.document.newFile;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.util.NewFileUtils;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;

public class NewFileComponentPresenter {
	NewFileComponent newFileComponent;
	private String documentTypeId;
	private SessionContext sessionContext;

	public NewFileComponentPresenter(NewFileComponent newFileComponent) {
		this.newFileComponent = newFileComponent;
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		newFileComponent.setSupportedExtensions(supportedExtensions);
	}

	public void documentTypeIdSet(String documentTypeId) {
		this.documentTypeId = documentTypeId;
		newFileComponent.setTemplateOptions(getTemplateOptions());
		newFileComponent.setTemplateFieldValue(null);
	}

	public String getDocumentTypeId() {
		return documentTypeId;
	}

	private List<Content> getTemplateOptions() {
		List<Content> templates = new ArrayList<>();
		if (documentTypeId != null) {
			// À valider avec francis pour l'appel au singloton static.

			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(
					sessionContext.getCurrentCollection(),
					newFileComponent.getAppLayerFactory());
			templates = rm.getDocumentType(documentTypeId).getTemplates();
		}
		return templates;
	}
}
