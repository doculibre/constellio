package com.constellio.app.modules.rm.ui.components.document.newFile;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.util.NewFileUtils;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;

public class NewFileComponentPresenter {
	NewFileComponent newFileComponent;
	private String documentTypeId;
	private SessionContext sessionContext;
	private ContentVersionToVOBuilder contentVersionToVOBuilder;
	private List<Content> contentsToChooseFrom;

	public NewFileComponentPresenter(NewFileComponent newFileComponent) {
		this.newFileComponent = newFileComponent;
		contentVersionToVOBuilder = new ContentVersionToVOBuilder(
				newFileComponent.getAppLayerFactory().getModelLayerFactory());
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		newFileComponent.setSupportedExtensions(supportedExtensions);
	}

	public void documentTypeIdSet(String documentTypeId) {
		this.documentTypeId = documentTypeId;
		contentsToChooseFrom = getTemplateOptions();
		newFileComponent.setTemplateOptions(contentsToChooseFrom);
		newFileComponent.setTemplateFieldValue((Content) null);
	}

	public Content getContentFromVO(ContentVersionVO contentVersionVO) {
		if (contentVersionVO == null) {
			return null;
		}

		for (Content content : contentsToChooseFrom) {
			if (content.getCurrentVersion().getHash().equals(contentVersionVO.getHash())) {
				return content;
			}
		}

		return null;
	}

	public ContentVersionVO getContentVO(Content content) {
		return contentVersionToVOBuilder.build(content, sessionContext);
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
